package ture.bank.dto;

import java.time.Instant;

/**
 * Фильтр для дат
 */
public class DateFilter {
    private Instant from;
    private Instant to;
    private Operator operator;

    public enum Operator {
        EQUALS, NOT_EQUALS, GREATER_THAN, LESS_THAN, BETWEEN
    }

    // Getters and Setters
    public Instant getFrom() {
        return from;
    }

    public void setFrom(Instant from) {
        this.from = from;
    }

    public Instant getTo() {
        return to;
    }

    public void setTo(Instant to) {
        this.to = to;
    }

    public Operator getOperator() {
        return operator != null ? operator : Operator.EQUALS;
    }

    public void setOperator(Operator operator) {
        this.operator = operator;
    }
}

