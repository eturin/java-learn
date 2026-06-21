package ture.courses.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "roles",
        comment = "Роли пользователей",
        indexes = {
                @Index(name = "idx_roles_name", columnList = "course_id,role_type_id", unique = true)
        }
)
public class Role {
    @Column(comment = "Идентификатор")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "course_id",
            nullable = false,
            comment = "Курс",
            foreignKey = @ForeignKey(name = "fk_course"))
    private Course course;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_type_id",
            nullable = false,
            comment = "Тип роли",
            foreignKey = @ForeignKey(name = "fk_role_type"))
    private RoleType roleType;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id",
            nullable = false,
            comment = "Пользователь",
            foreignKey = @ForeignKey(name = "fk_user"))
    private User user;



}
