package io.github.nnhieu.jpaspecbuilder.spring.operator.registry;

import io.github.nnhieu.jpaspecbuilder.core.model.FilterOperator;
import io.github.nnhieu.jpaspecbuilder.spring.operator.Operator;

public interface OperatorRegistry {
    String OPERATOR_PACKAGE = "io.github.nnhieu.jpaspecbuilder.spring.operator";

    void register(Operator operator);

    Operator find(FilterOperator operator);
}
