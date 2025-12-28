package ture.bank.dto;

import java.util.List;

/**
 * DTO для гибкой фильтрации пользователей
 */
public class UserSearchCriteria {
    private List<StringFilter> login;
    private List<StringFilter> fio;
    private List<RoleFilter> role;
    private DateFilter createdAt;
    private DateFilter updatedAt;
    private Boolean deleted;
    private Integer page;
    private Integer size;
    private String sortBy;
    private String sortDirection;

    public UserSearchCriteria() {}

    // Getters and Setters
    public List<StringFilter> getLogin() {
        return login;
    }

    public void setLogin(List<StringFilter> login) {
        this.login = login;
    }

    public List<StringFilter> getFio() {
        return fio;
    }

    public void setFio(List<StringFilter> fio) {
        this.fio = fio;
    }

    public List<RoleFilter> getRole() {
        return role;
    }

    public void setRole(List<RoleFilter> role) {
        this.role = role;
    }

    public DateFilter getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(DateFilter createdAt) {
        this.createdAt = createdAt;
    }

    public DateFilter getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(DateFilter updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    public Integer getPage() {
        return page != null ? page : 0;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getSize() {
        return size != null ? size : 10;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public String getSortBy() {
        return sortBy != null ? sortBy : "id";
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    public String getSortDirection() {
        return sortDirection != null ? sortDirection : "asc";
    }

    public void setSortDirection(String sortDirection) {
        this.sortDirection = sortDirection;
    }
}





