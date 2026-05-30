package io.github.nnhieu.jpaspecbuilder.spring.operator;

import io.github.nnhieu.jpaspecbuilder.core.model.FilterOperator;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;

public interface Operator {

    boolean supports(FilterOperator operator);

    Predicate toPredicate(FilterOperator operator, Path<?> path, CriteriaBuilder cb, Object value);
}
