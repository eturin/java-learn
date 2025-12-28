package ture.bank.entity;

import jakarta.persistence.*;

/**
 * Сущность, представляющая роль в системе.
 * <p>
 * Java-класс, который представляет таблицу {@code roles} в базе данных.
 * Этот класс будет "отражением" таблицы в базе данных. Какое поле в классе - такой столбец в таблице..
 * </p>
 */
@Entity
@Table(name = "roles")
public class Role {
    /**
     * Идентификатор роли
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Наименование роли
     */
    @Column(name = "name", nullable = false)
    private String name;

    /**
     * Описание роли
     */
    @Column(name = "description", nullable = false)
    private String description;

    /**
     * Конструктор без параметров - требование спецификации JPA
     */
    public Role() {}

    /**
     * Получение идентификатора роли
     * @return Long
     */
    public Long getId() {
        return id;
    }

    /**
     * Получение наименования роли
     * @return String
     */
    public String getName() {
        return name;
    }

    /**
     * Получение описания роли
     * @return String
     */
    public String getDescription() {
        return description;
    }
}
