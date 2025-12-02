package ture.app.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ture.app.entity.Account;
import ture.app.repository.AccountRepository;
import ture.app.repository.UserRepository;

import java.util.Optional;

// Создаем класс AccountService, который будет содержать основную логику работы со счетами.
// Для чего это нужно:
// - Service слой содержит бизнес-логику приложения
// - Отделяет контроллеры (которые принимают запросы) от репозиториев (которые работают с БД)
// - Здесь мы можем добавлять проверки, преобразования данных и другую логику
@Service
public class AccountService {
    private static final Logger logger = LogManager.getLogger(AccountService.class);

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private UserRepository userRepository;

    // добавление счёта
    public Account create(Long userId, String name) {
        logger.info(String.format("Creating account for user %s '%s'", userId, name));
        var usr = userRepository.findById(userId);
        if(usr.isEmpty()) throw new RuntimeException("User not found");

        var account = new Account(usr.get(), name);
        return accountRepository.save(account);
    }
    // получение счета по ID
    public Optional<Account> getById(Long id) {
        logger.info(String.format("Retrieving account with id '%s'", id));
        return accountRepository.findById(id);
    }
    // получение остатка по счёту
    public String getAmount(Long accountId) {
        logger.info(String.format("Getting amount for account with id '%s'", accountId));
        var account = accountRepository.findById(accountId);
        if(account.isEmpty()) throw new RuntimeException("Account not found");
        return String.format("%.2f", account.get().getAmount()/100.0);
    }
    // изменение наименования
    public Boolean setAccountName(Long usrId,
                                  Long accountId,
                                  String name) {
        logger.info(String.format("Updating account`s name with id '%s' to '%s'", accountId, name));
        var account = accountRepository.findById(accountId);
        if(account.isEmpty()) throw new RuntimeException("Account not found");

        var acc = account.get();
        if(acc.getUser().getId() == usrId) acc.setName(name);
        else throw new RuntimeException("Account is not your");
        accountRepository.save(acc);
        return true;
    }
}
