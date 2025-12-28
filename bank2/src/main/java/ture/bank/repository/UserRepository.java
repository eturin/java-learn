package ture.bank.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ture.bank.entity.User;

import java.util.Optional;

/**
 * Интерфейс UserRepository будет отвечать за операции с базой данных.
 * Для чего это нужно:
 *  - Repository - это прослойка между приложением и базой данных
 *  - Spring Data JPA автоматически создает реализации методов
 *  - Мы получаем готовые методы для CRUD операций (Create, Read, Update, Delete)
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    // JPQL - в запросе сущности java приложения, а не таблицы СУБД
    @Query("SELECT u FROM User u WHERE u.login = :login")
    Optional<User> findByLogin(@Param("login") String login);

    /**
     * Поиск пользователей с пагинацией
     * (JPQL - самостоятельно реализует метод)
     * @param pageable объект пагинации
     * @return страница пользователей
     */
    Page<User> findAll(Pageable pageable);
}
