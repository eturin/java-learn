package ture.app.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ture.app.entity.Account;
import ture.app.entity.Transaction;
import ture.app.repository.AccountRepository;
import ture.app.native_sql.AppLocks;
import ture.app.repository.TransactionRepository;


// Создаем класс TransactionService, который будет содержать основную логику переводов между пользователями.
// Для чего это нужно:
// - Service слой содержит бизнес-логику приложения
// - Отделяет контроллеры (которые принимают запросы) от репозиториев (которые работают с БД)
// - Здесь мы можем добавлять проверки, преобразования данных и другую логику
@Service
public class TransactionService {
    private static final Logger logger = LogManager.getLogger(TransactionService.class);

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private AppLocks appLocks;

    @Autowired
    private AccountRepository accountRepository;

    @Transactional
    public Transaction create(Long fromAccID, Long toAccID, Integer amount) {
        var from_acc = accountRepository.findById(fromAccID);
        var to_acc =   accountRepository.findById(toAccID);
        if(from_acc.isEmpty())
            throw new RuntimeException("From Account cannot be null");
        if(to_acc.isEmpty())
            throw new RuntimeException("To Account cannot be null");

        return create(from_acc.get(), to_acc.get(), amount);
    }
        // создание перевода
    @Transactional
    public Transaction create(Account from_acc, Account to_acc, Integer amount) {
        logger.info(String.format("Creating transaction: %s -> %s = %.2f", from_acc.getId(), to_acc.getId(), amount/100.0));
        if(amount <= 0)
            throw new RuntimeException("Amount must be greater than zero");
        if(from_acc == null)
            throw new RuntimeException("From Account cannot be null");
        if(to_acc == null)
            throw new RuntimeException("To Account cannot be null");

        //формируем массив блокируемых объектов
        Object[] m = {from_acc,to_acc};
        //блокируем
        appLocks.lock(m);

        //перечитываем из СУБД
        entityManager.refresh(from_acc);
        entityManager.refresh(to_acc);

        //проверки
        if(from_acc.getBlockedAt() != null)
            throw new RuntimeException("From Account is blocked");
        if(from_acc.getClosedAt() != null)
            throw new RuntimeException("From Account is closed");
        if(to_acc.getClosedAt() != null)
            throw new RuntimeException("To Account is closed");

        //меняем источник
        from_acc.addAmount(-amount);
        if(from_acc.getAmount() < 0)
            throw new RuntimeException("From Amount must be greater than amount");
        accountRepository.save(from_acc);

        //меняем приёмник
        to_acc.addAmount(amount);
        accountRepository.save(to_acc);

        //пишем лог операций
        var transaction = new Transaction(from_acc, to_acc, amount);
        transaction = transactionRepository.save(transaction);

        // Обновляем объект из БД, чтобы получить created_at
        entityManager.refresh(transaction);

        return transaction;
    }
}
