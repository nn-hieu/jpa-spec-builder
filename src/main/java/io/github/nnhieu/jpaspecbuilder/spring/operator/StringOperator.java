package io.github.nnhieu.jpaspecbuilder.spring.operator;

import io.github.nnhieu.jpaspecbuilder.core.model.FilterOperator;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;

import java.util.Locale;

public class StringOperator implements Operator {

    @Override
    public boolean supports(FilterOperator operator) {
        return operator == FilterOperator.LIKE
                || operator == FilterOperator.ILIKE
                || operator == FilterOperator.STARTS_WITH
                || operator == FilterOperator.ENDS_WITH
                || operator == FilterOperator.CONTAINS;
    }

    @Override
    public Predicate toPredicate(FilterOperator operator, Path<?> path, CriteriaBuilder cb, Object value) {
        if (value == null) {
            return cb.disjunction();
        }
        String pattern = this.pattern(operator, String.valueOf(value));
        Expression<String> expression = path.as(String.class);
        if (operator == FilterOperator.ILIKE) {
            return cb.like(cb.lower(expression), pattern.toLowerCase(Locale.ROOT), '\\');
        }
        return cb.like(expression, pattern, '\\');
    }

    private String pattern(FilterOperator operator, String value) {
        return switch (operator) {
            case STARTS_WITH -> this.escapeWildcards(value) + "%";
            case ENDS_WITH -> "%" + this.escapeWildcards(value);
            case LIKE, ILIKE, CONTAINS -> "%" + this.escapeWildcards(value) + "%";
            default -> throw new IllegalArgumentException("Unsupported string operator: " + operator);
        };
    }

    private String escapeWildcards(String value) {
        return value.replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
    }
}
