package io.github.nnhieu.jpaspecbuilder.spring.operator;

import io.github.nnhieu.jpaspecbuilder.core.model.FilterOperator;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class CollectionOperator implements Operator {

    @Override
    public boolean supports(FilterOperator operator) {
        return operator == FilterOperator.IN
                || operator == FilterOperator.IN_IGNORE_CASE
                || operator == FilterOperator.NOT_IN
                || operator == FilterOperator.NOT_IN_IGNORE_CASE;
    }

    @Override
    public Predicate toPredicate(FilterOperator operator, Path<?> path, CriteriaBuilder cb, Object value) {
        Collection<?> collection = this.collection(value);
        if (collection.isEmpty()) {
            return this.isInOperator(operator) ? cb.disjunction() : cb.conjunction();
        }
        Predicate predicate;
        if (this.isIgnoreCase(operator)) {
            if (!collection.stream().allMatch(String.class::isInstance)) {
                throw new IllegalArgumentException("Collection must contain only String values for IN_IGNORE_CASE/NOT_IN_IGNORE_CASE operator");
            }
            Collection<String> values = collection.stream()
                    .filter(Objects::nonNull)
                    .map(String.class::cast)
                    .toList();
            predicate = this.ignoreCasePredicate(path, cb, values);
        } else {
            predicate = path.in(collection);
        }
        return this.isInOperator(operator) ? predicate : cb.not(predicate);
    }

    private Collection<?> collection(Object value) {
        if (value instanceof Collection<?> c) {
            return c;
        }
        if (value instanceof Object[] array) {
            return List.of(array);
        }
        throw new IllegalArgumentException("Value must be a collection or an array for IN/NOT_IN operator");
    }

    private Predicate ignoreCasePredicate(Path<?> path, CriteriaBuilder cb, Collection<String> collection) {
        List<String> values = collection.stream()
                .map(value -> value.toLowerCase(Locale.ROOT))
                .toList();
        if (values.isEmpty()) {
            return cb.disjunction();
        }
        Expression<String> expression = cb.lower(path.as(String.class));
        return expression.in(values);
    }

    private boolean isIgnoreCase(FilterOperator operator) {
        return operator == FilterOperator.IN_IGNORE_CASE || operator == FilterOperator.NOT_IN_IGNORE_CASE;
    }

    private boolean isInOperator(FilterOperator operator) {
        return operator == FilterOperator.IN || operator == FilterOperator.IN_IGNORE_CASE;
    }
}
