package io.github.nnhieu.jpaspecbuilder.spring.operator;

import io.github.nnhieu.jpaspecbuilder.core.model.FilterOperator;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;

public class NullOperator implements Operator {

    @Override
    public boolean supports(FilterOperator operator) {
        return operator == FilterOperator.IS_NULL || operator == FilterOperator.IS_NOT_NULL;
    }

    @Override
    public Predicate toPredicate(FilterOperator operator, Path<?> path, CriteriaBuilder cb, Object ignored) {
        Predicate predicate = cb.isNull(path);
        return operator == FilterOperator.IS_NULL ? predicate : cb.not(predicate);
    }
}
