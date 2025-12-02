package ture.app.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.*;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

// Создаем Java-класс, который будет представлять таблицу "transactions" в базе данных.
// Этот класс будет "отражением" таблицы в базе данных. Какое поле в классе - такой столбец в таблице.

@Entity
@Table(name = "transactions", indexes = {
        @Index(name = "idx_transactions_created_at", columnList = "created_at")
})
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "created_at",
            updatable = false,
            insertable = false,
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_acc_id", nullable = false, foreignKey = @ForeignKey(name = "accounts"))
    private Account fromAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_acc_id", nullable = false, foreignKey = @ForeignKey(name = "accounts"))
    private Account toAccount;

    @Column(name = "amount")
    private Integer amount;

    public Transaction() {}
    public Transaction(Account fromAccount, Account toAccount, Integer amount) {
        this.fromAccount = fromAccount;
        this.toAccount = toAccount;
        this.amount = amount;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Account getFromAccount() {
        return fromAccount;
    }

    public void setFromAccount(Account fromAccount) {
        this.fromAccount = fromAccount;
    }

    public Account getToAccount() {
        return toAccount;
    }

    public void setToAccount(Account toAccount) {
        this.toAccount = toAccount;
    }

    public Integer getAmount() {
        return amount;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }
}
