package ture.app.dto;

import ture.app.entity.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


public class UserDTO {
    private Long id;
    private String name;
    private String email;
    private LocalDateTime createdAt;
    private List<AccountDTO> accounts;

    public UserDTO() {}
    public UserDTO(Long id,
                   String name,
                   String email,
                   LocalDateTime createdAt,
                   List<AccountDTO> accounts) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.createdAt = createdAt;
        this.accounts = accounts;
    }
    public UserDTO(User user) {
        this.id = user.getId();
        this.name = user.getName();
        this.email = user.getEmail();
        this.createdAt = user.getCreatedAt();
        this.accounts = user.getAccounts()
                            .values()
                            .stream()
                            .map(AccountDTO::new)
                            .collect(Collectors.toList());
    }
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<AccountDTO> getAccounts() {
        return accounts;
    }

    public void setAccounts(List<AccountDTO> accounts) {
        this.accounts = accounts;
    }
}
