package io.github.nnhieu.jpaspecbuilder.spring.operator;

import io.github.nnhieu.jpaspecbuilder.core.model.FilterOperator;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;

import java.util.Objects;

public class EqualityOperator implements Operator {

    @Override
    public boolean supports(FilterOperator operator) {
        return operator == FilterOperator.EQUALS || operator == FilterOperator.NOT_EQUALS;
    }

    @Override
    public Predicate toPredicate(FilterOperator operator, Path<?> path, CriteriaBuilder cb, Object value) {
        Objects.requireNonNull(value);
        Predicate predicate = cb.equal(path, value);
        return operator == FilterOperator.EQUALS ? predicate : cb.not(predicate);
    }
}
