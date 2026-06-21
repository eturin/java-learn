package ture.courses.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "role_types",
       comment = "Роли пользователей",
       indexes = {
               @Index(name = "idx_role_types_name", columnList = "name", unique = true)
       }
)
public class RoleType {
    @Column(comment = "Идентификатор")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(comment = "Краткое наименонвание роли", unique = true)
    private String name;

    public RoleType(String name) {
        this.name = name;
    }
}
