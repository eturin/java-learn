package ture.courses.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "courses",
       comment = "Курсы",
        indexes = {
                @Index(name = "idx_courses_name", columnList = "name", unique = true)
        })
public class Course {
    @Id
    @Column(comment = "Идентификатор")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(comment = "Наименование курса", unique = true)
    private String name;

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
