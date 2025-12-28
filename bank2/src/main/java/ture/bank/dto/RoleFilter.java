package ture.bank.dto;

/**
 * Фильтр для ролей
 */
public class RoleFilter {
    private Long id;
    private String name;
    private StringFilter nameFilter;
    private Operator operator;
    private LogicalOperator logicalOperator;

    public enum Operator {
        EQUALS, NOT_EQUALS, IN, NOT_IN
    }

    public enum LogicalOperator {
        AND, OR
    }

    // Getters and Setters
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

    public StringFilter getNameFilter() {
        return nameFilter;
    }

    public void setNameFilter(StringFilter nameFilter) {
        this.nameFilter = nameFilter;
    }

    public Operator getOperator() {
        return operator != null ? operator : Operator.EQUALS;
    }

    public void setOperator(Operator operator) {
        this.operator = operator;
    }

    public LogicalOperator getLogicalOperator() {
        return logicalOperator != null ? logicalOperator : LogicalOperator.AND;
    }

    public void setLogicalOperator(LogicalOperator logicalOperator) {
        this.logicalOperator = logicalOperator;
    }
}
