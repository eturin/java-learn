package ture.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ture.app.entity.Account;

// Создаем интерфейс AccountRepository, который будет отвечать за операции с базой данных.
// Для чего это нужно:
// - Repository - это прослойка между приложением и базой данных
// - Spring Data JPA автоматически создает реализации методов
// - Мы получаем готовые методы для CRUD операций (Create, Read, Update, Delete)
@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    // задаём специфический запрос
    @Modifying
    @Query("UPDATE Account a SET a.amount = a.amount WHERE a.id = :id")
    void touch(@Param("id") Long id);
}
