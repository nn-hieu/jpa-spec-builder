package io.github.nnhieu.jpaspecbuilder.spring.operator;

import io.github.nnhieu.jpaspecbuilder.core.exception.QueryBuildException;
import io.github.nnhieu.jpaspecbuilder.core.model.FilterOperator;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;

import java.util.Collection;
import java.util.Iterator;

public class ComparableOperator implements Operator {

    @Override
    public boolean supports(FilterOperator operator) {
        return operator == FilterOperator.GREATER_THAN
                || operator == FilterOperator.GREATER_THAN_EQUAL
                || operator == FilterOperator.LESS_THAN
                || operator == FilterOperator.LESS_THAN_EQUAL
                || operator == FilterOperator.BETWEEN;
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public Predicate toPredicate(FilterOperator operator, Path<?> path, CriteriaBuilder cb, Object value) {
        return switch (operator) {
            case GREATER_THAN -> cb.greaterThan(this.comparablePath(path), this.comparable(value));
            case GREATER_THAN_EQUAL -> cb.greaterThanOrEqualTo(this.comparablePath(path), this.comparable(value));
            case LESS_THAN -> cb.lessThan(this.comparablePath(path), this.comparable(value));
            case LESS_THAN_EQUAL -> cb.lessThanOrEqualTo(this.comparablePath(path), this.comparable(value));
            case BETWEEN -> {
                if (!(value instanceof Collection<?> collection) || collection.size() != 2) {
                    throw new IllegalArgumentException("Value must be an collection of two elements for BETWEEN operator");
                }
                Iterator<?> iterator = collection.iterator();
                Comparable first = this.comparable(iterator.next());
                Comparable second = this.comparable(iterator.next());
                if (!first.getClass().equals(second.getClass())) {
                    throw new IllegalArgumentException("Both values must have the same type for BETWEEN operator");
                }
                Comparable lower = first.compareTo(second) <= 0 ? first : second;
                Comparable upper = first.compareTo(second) <= 0 ? second : first;
                yield cb.between(this.comparablePath(path), lower, upper);
            }
            default -> throw new QueryBuildException("Unsupported comparable operator: " + operator);
        };
    }

    private Expression<Comparable> comparablePath(Path<?> path) {
        return (Expression) path;
    }

    @SuppressWarnings("rawtypes")
    private Comparable comparable(Object value) {
        if (value instanceof Comparable c) {
            return c;
        }
        throw new IllegalArgumentException("Value must be comparable for operator");
    }
}
