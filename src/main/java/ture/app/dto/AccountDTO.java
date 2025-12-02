package ture.app.dto;

import ture.app.entity.Account;

import java.time.LocalDateTime;

public class AccountDTO {
    private Long id;
    private String name;
    private String amount;
    private Long userId;
    private LocalDateTime createdAt;
    private LocalDateTime blockedAt;
    private LocalDateTime closedAt;

    public AccountDTO() {}
    public AccountDTO(Long id,
                      String name,
                      Integer amount,
                      Long userId,
                      LocalDateTime createdAt,
                      LocalDateTime blockedAt,
                      LocalDateTime closedAt) {
        this.id = id;
        this.name = name;
        this.amount = String.format("%.2f", amount/100.0);
        this.userId = userId;
        this.createdAt = createdAt;
        this.blockedAt = blockedAt;
        this.closedAt = closedAt;
    }
    public AccountDTO(Account account) {
        this.id = account.getId();
        this.name = account.getName();
        this.amount = String.format("%.2f", account.getAmount()/100.0);
        this.userId = account.getUser().getId();
        this.createdAt = account.getCreatedAt();
        this.blockedAt = account.getBlockedAt();
        this.closedAt = account.getClosedAt();
    }


    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setBlockedAt(LocalDateTime blockedAt) {
        this.blockedAt = blockedAt;
    }

    public void setClosedAt(LocalDateTime closedAt) {
        this.closedAt = closedAt;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getAmount() {
        return amount;
    }

    public Long getUserId() {
        return userId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getBlockedAt() {
        return blockedAt;
    }

    public LocalDateTime getClosedAt() {
        return closedAt;
    }
}
