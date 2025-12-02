package ture.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ture.app.entity.Account;
import ture.app.entity.Transaction;

import java.util.Optional;

// Создаем интерфейс TransactionRepository, который будет отвечать за операции с базой данных.
// Для чего это нужно:
// - Repository - это прослойка между приложением и базой данных
// - Spring Data JPA автоматически создает реализации методов
// - Мы получаем готовые методы для CRUD операций (Create, Read, Update, Delete)
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {


    // Эти методы Spring реализует автоматически!
    Optional<Transaction> findByFromAccount(Account account);
    Optional<Transaction> findByToAccount(Account account);
}
