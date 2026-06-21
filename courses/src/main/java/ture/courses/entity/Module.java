package ture.courses.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "modules",
        comment = "Модули курсов",
        indexes = {
                @Index(name = "idx_modules_name", columnList = "course_id,name", unique = true)
        })
public class Module {
    @Id
    @Column(comment = "Идентификатор")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "course_id",
            nullable = false,
            comment = "Курс",
            foreignKey = @ForeignKey(name = "fk_course"))
    private Course course;

    @Column(comment = "Наименование модуля")
    private String name;

    @Column(comment = "Описание")
    private String description;

    @CreationTimestamp
    @Column(comment = "Создание", nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "created_user_id",
            nullable = false,
            comment = "Создал",
            foreignKey = @ForeignKey(name = "fk_created_user"))
    private User createdBy;

    @UpdateTimestamp
    @Column(comment = "Обновление")
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "updated_user_id",
            nullable = false,
            comment = "Обновил",
            foreignKey = @ForeignKey(name = "fk_updated_user"))
    private User updatedBy;
}
