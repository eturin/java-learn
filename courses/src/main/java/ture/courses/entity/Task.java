package ture.courses.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "tasks",
        comment = "Задание в составе темы",
        indexes = {
                @Index(name = "idx_tasks_name", columnList = "topic_id,name", unique = true)
        })
public class Task {
    @Id
    @Column(comment = "Идентификатор")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "topic_id",
            nullable = false,
            comment = "Тема",
            foreignKey = @ForeignKey(name = "fk_topic"))
    private Topic topic;

    @Column(comment = "Задание")
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
