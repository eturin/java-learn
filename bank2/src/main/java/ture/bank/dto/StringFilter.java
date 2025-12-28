package ture.bank.dto;

/**
 * Фильтр для строковых полей
 */
public class StringFilter {
    private String value;
    private Operator operator;
    private LogicalOperator logicalOperator; // AND или OR для связи с другими фильтрами того же поля

    public enum Operator {
        EQUALS,           // точное совпадение
        NOT_EQUALS,       // не равно
        CONTAINS,         // содержит подстроку
        STARTS_WITH,      // начинается с
        ENDS_WITH,        // заканчивается на
        IN,               // в списке значений (через запятую)
        NOT_IN            // не в списке значений
    }

    public enum LogicalOperator {
        AND, OR
    }

    public StringFilter() {}

    public StringFilter(String value, Operator operator) {
        this.value = value;
        this.operator = operator;
        this.logicalOperator = LogicalOperator.AND;
    }

    // Getters and Setters
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
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