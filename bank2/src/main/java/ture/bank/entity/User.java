package ture.bank.entity;
import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.util.Collection;
import java.util.Collections;


/**
 * Сущность, представляющая пользователя в системе.
 * <p>
 * Java-класс, который представляет таблицу {@code users} в базе данных.
 * Этот класс будет "отражением" таблицы в базе данных. Какое поле в классе - такой столбец в таблице..
 * </p>
 */
@Entity
@Table(name = "users")
public class User implements UserDetails {
    /**
     * Идентификатор роли
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Логин пользователя (уникальный)
     */
    @Column(name = "login", nullable = false, unique = true, length = 50)
    private String login;

    /**
     * ФИО пользователя
     */
    @Column(name = "fio", length = 255)
    private String fio;

    /**
     * Роль пользователя
     * @see Role
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = false, foreignKey = @ForeignKey(name = "fk_users_role_id"))
    private Role role;

    /**
     * Хеш пароля (BCrypt)
     */
    @Column(name = "pwd_hash", nullable = false, length = 255)
    private String passwordHash;

    /**
     * Дата и время создания пользователя
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /**
     * Дата и время последнего обновления пользователя
     */
    @Column(name = "updated_at")
    private Instant updatedAt;

    /**
     * Дата и время удаления пользователя (мягкое удаление)
     */
    @Column(name = "deleted_at")
    private Instant deletedAt;

    /**
     * Конструктор без параметров - требование спецификации JPA
     */
    public User() {}

    /**
     * Приватная функция заполнения инкапсулирующая инициализацию экземпляра
     * @param login
     * @param fio
     * @param role
     * @param passwordHash
     */
    private void fill(String login, String fio, Role role, String passwordHash) {
        this.login = login;
        this.fio = fio;
        this.role = role;
        this.passwordHash = passwordHash;
    }

    /**
     * Конструктор с основными параметрами
     * @param login логин пользователя
     * @param role роль пользователя
     * @param passwordHash хеш пароля
     */
    public User(String login, Role role, String passwordHash) {
        fill(login, null, role, passwordHash);
    }

    /**
     * Конструктор с основными параметрами
     * @param login логин пользователя
     * @param fio ФИО пользователя
     * @param role роль пользователя
     * @param passwordHash хеш пароля
     */
    public User(String login, String fio, Role role, String passwordHash) {
        fill(login, fio, role, passwordHash);
    }

    /**
     * Callback метод, вызываемый перед сохранением новой записи
     */
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now(); // UTC время
        }
    }

    /**
     * Получение идентификатора пользователя
     * @return Long
     */
    public Long getId() {
        return id;
    }

    /**
     * Получение логина пользователя
     * @return String
     */
    public String getLogin() {
        return login;
    }

    /**
     * Получение ФИО пользователя
     * @return String
     */
    public String getFio() {
        return fio;
    }

    /**
     * Получение роли пользователя
     * @return Role
     */
    public Role getRole() {
        return role;
    }

    /**
     * Получение хеша пароля пользователя
     * @return String
     */
    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    /**
     * Получение даты добавления пользователя
     * @return java.time.Instant
     */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /**
     * Получение даты изменения свойств пользователя
     * @return java.time.Instant
     */
    public Instant getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Получение даты удаления пользователя
     * @return java.time.Instant
     */
    public Instant getDeletedAt() {
        return deletedAt;
    }

    /**
     * Проверка, удален ли пользователь
     * @return true если пользователь удален
     */
    public boolean isDeleted() {
        return deletedAt != null;
    }

    /**
     * Изменение ФИО пользователя
     * @param fio ФИО пользователя
     */
    public void setFio(String fio) {
        this.fio = fio;
    }

    /**
     * Изменение роли пользователя
     * @param role роль пользователя
     */
    public void setRole(Role role) {
        this.role = role;
    }

    // ====== Реализация UserDetails ======

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Преобразуем роль в формат Spring Security
        return Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + role.getName())
        );
    }

    @Override
    public String getPassword() {
        return this.passwordHash;
    }

    @Override
    public String getUsername() {
        return this.login;
    }

    @Override
    public boolean isAccountNonExpired() {
        return this.deletedAt == null;
    }

    @Override
    public boolean isAccountNonLocked() {
        return this.deletedAt == null;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return this.deletedAt == null;
    }

    @Override
    public boolean isEnabled() {
        return this.deletedAt == null;
    }
}
