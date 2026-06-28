package ture.courses.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "users",
       comment = "Пользователи",
       schema = "usr",
       indexes = {
               @Index(name = "idx_users_email", columnList = "email", unique = true),
               @Index(name = "idx_users_name", columnList = "name", unique = true),
               @Index(name = "idx_users_full_name", columnList = "full_name", unique = true)
       }
)
public class User {
    @Id
    @Column(comment = "Идентификатор")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(comment = "Имя пользователя", nullable = false, length = 50, unique = true)
    private String name;

    @Column(comment = "Полное имя", unique = true)
    private String fullName;

    @Column(comment = "Электронная почта", nullable = false, unique = true)
    private String email;

    @Column(comment = "Признак администратора")
    private Boolean isAdmin;

    @CreationTimestamp
    @Column(comment = "Создан", nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(comment = "Обновлён")
    private LocalDateTime updatedAt;

    @Column(comment = "Заблокирован")
    private LocalDateTime blockedAt;
}
