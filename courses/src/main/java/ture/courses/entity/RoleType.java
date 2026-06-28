package ture.courses.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import ture.courses.enums.RoleName;

@Data
@AllArgsConstructor
@Entity
@Table(name = "role_types",
       comment = "Роли пользователей",
       indexes = {
               @Index(name = "idx_role_types_name", columnList = "role_name", unique = true)
       }
)
public class RoleType {
    @Column(comment = "Идентификатор")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(comment = "Краткое наименонвание роли", unique = true)
    private RoleName roleName;

    public RoleType(RoleName roleName) {
        this.roleName = roleName;
    }
}
