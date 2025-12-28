package ture.bank.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ture.bank.entity.Role;

import java.util.Optional;

/**
 * Интерфейс RoleRepository будет отвечать за операции с базой данных.
 * Для чего это нужно:
 *  - Repository - это прослойка между приложением и базой данных
 *  - Spring Data JPA автоматически создает реализации методов
 *  - Мы получаем готовые методы для CRUD операций (Create, Read, Update, Delete)
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    /**
     * Spring реализует автоматически этот метод
     * @param name имя роли для поиска (не должно быть {@code null})
     * @return {@link Optional} содержащий роль если найдена, иначе пустой {@link Optional}
     */
    Optional<Role> findByName(String name);
}
