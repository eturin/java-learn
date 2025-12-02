package ture.app.repository;

import ture.app.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

// Создаем интерфейс UserRepository, который будет отвечать за операции с базой данных.
// Для чего это нужно:
// - Repository - это прослойка между приложением и базой данных
// - Spring Data JPA автоматически создает реализации методов
// - Мы получаем готовые методы для CRUD операций (Create, Read, Update, Delete)
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // Эти методы Spring реализует автоматически!
    Optional<User> findByName(String name);
    Optional<User> findByEmail(String email);
}
