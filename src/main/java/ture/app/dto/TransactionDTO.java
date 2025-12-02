package ture.app.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import ture.app.entity.Transaction;

import java.time.LocalDateTime;

public class TransactionDTO {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long id;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private LocalDateTime createdAt;

    private AccountDTO from_acc;
    private AccountDTO to_acc;
    private String amount;

    public TransactionDTO() {}
    public TransactionDTO(Long id,
                          LocalDateTime createdAt,
                          AccountDTO from_acc,
                          AccountDTO to_acc,
                          Integer amount) {
        this.id = id;
        this.createdAt = createdAt;
        this.from_acc = from_acc;
        this.to_acc = to_acc;
        this.amount = String.format("%.2f",amount/100.0);
    }
    public TransactionDTO(Transaction transaction) {
        this.id = transaction.getId();
        this.createdAt = transaction.getCreatedAt();
        this.from_acc = new AccountDTO(transaction.getFromAccount());
        this.to_acc = new AccountDTO(transaction.getToAccount());
        this.amount = String.format("%.2f",transaction.getAmount()/100.0);
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

    public AccountDTO getFrom_acc() {
        return from_acc;
    }

    public void setFrom_acc(AccountDTO from_acc) {
        this.from_acc = from_acc;
    }

    public AccountDTO getTo_acc() {
        return to_acc;
    }

    public void setTo_acc(AccountDTO to_acc) {
        this.to_acc = to_acc;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }
}
