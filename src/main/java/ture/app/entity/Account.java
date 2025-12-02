package ture.app.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

// Создаем Java-класс, который будет представлять таблицу "accounts" в базе данных.
// Этот класс будет "отражением" таблицы в базе данных. Какое поле в классе - такой столбец в таблице.

@Entity
@Table(name = "accounts",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_account_user_name",
                        columnNames = {"user_id", "name"}
                )
        })
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "users"))
    private User user;

    @Column(name = "amount", nullable = false)
    private Integer amount;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "created_at",
            updatable = false,
            insertable = false,
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @Column(name = "closed_at",
            updatable = false,
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime closedAt;

    @Column(name = "blocked_at",
            updatable = false,
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime blockedAt;

    public User getUser() {
        return user;
    }
    public Long getId() {
        return id;
    }

    public Account() {}
    public Account(User user, String name) {
        this.user = user;
        this.name = name;
        this.amount = 0;
    }

    public Integer getAmount() {
        return amount;
    }

    public void setName(String name) {
        this.name = name;
    }
    public String getName() {
        return name;
    }
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public LocalDateTime getBlockedAt() {
        return blockedAt;
    }
    public void block() {
        this.blockedAt = LocalDateTime.now();
    }
    public void unblock() {
        this.blockedAt = null;
    }
    public void close() {
        this.closedAt = LocalDateTime.now();
    }
    public LocalDateTime getClosedAt() {
        return closedAt;
    }

    public void addAmount(Integer amount) {
        this.amount += amount;
    }

    @Override
    public String toString() {
        return "Account{" +
                "id=" + id +
                ", user=" + user +
                ", amount=" + amount +
                ", name='" + name + '\'' +
                ", createdAt=" + createdAt +
                ", createdAt=" + blockedAt +
                ", createdAt=" + closedAt +
                '}';
    }
}
