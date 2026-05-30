package io.github.nnhieu.jpaspecbuilder.core.query;

import io.github.nnhieu.jpaspecbuilder.core.model.NullHandling;
import io.github.nnhieu.jpaspecbuilder.core.model.QueryModel;
import io.github.nnhieu.jpaspecbuilder.core.model.SortDirection;

import java.util.Collection;
import java.util.function.Consumer;

public interface QueryBuilder<T> {

    static <T> QueryBuilder<T> builder(Class<T> entityClass) {
        return new JpaExecutableQuery<>(entityClass);
    }

    QueryBuilder<T> equals(String path, Object value);

    QueryBuilder<T> notEquals(String path, Object value);

    QueryBuilder<T> like(String path, String value);

    QueryBuilder<T> ilike(String path, String value);

    QueryBuilder<T> startsWith(String path, String value);

    QueryBuilder<T> endsWith(String path, String value);

    QueryBuilder<T> contains(String path, String value);

    <V extends Comparable<? super V>> QueryBuilder<T> between(String path, V from, V to);

    QueryBuilder<T> in(String path, Collection<?> values);

    <V> QueryBuilder<T> in(String path, V[] values);

    QueryBuilder<T> inIgnoreCase(String path, Collection<String> values);

    QueryBuilder<T> notIn(String path, Collection<?> values);

    <V> QueryBuilder<T> notIn(String path, V[] values);

    QueryBuilder<T> notInIgnoreCase(String path, Collection<String> values);

    QueryBuilder<T> greaterThan(String path, Comparable<?> value);

    QueryBuilder<T> greaterThanOrEqual(String path, Comparable<?> value);

    QueryBuilder<T> lessThan(String path, Comparable<?> value);

    QueryBuilder<T> lessThanOrEqual(String path, Comparable<?> value);

    QueryBuilder<T> isNull(String path);

    QueryBuilder<T> isNotNull(String path);

    QueryBuilder<T> join(String path);

    QueryBuilder<T> join(String path, Consumer<QueryBuilder<T>> group);

    QueryBuilder<T> leftJoin(String path);

    QueryBuilder<T> leftJoin(String path, Consumer<QueryBuilder<T>> group);

    QueryBuilder<T> rightJoin(String path);

    QueryBuilder<T> rightJoin(String path, Consumer<QueryBuilder<T>> group);

    QueryBuilder<T> and(Consumer<QueryBuilder<T>> group);

    QueryBuilder<T> or(Consumer<QueryBuilder<T>> group);

    QueryBuilder<T> sortBy(String path, SortDirection direction);

    QueryBuilder<T> sortBy(String path, SortDirection direction, NullHandling nullHandling);

    default QueryBuilder<T> sortAsc(String path) {
        return this.sortBy(path, SortDirection.ASC);
    }

    default QueryBuilder<T> sortAsc(String path, NullHandling nullHandling) {
        return this.sortBy(path, SortDirection.ASC, nullHandling);
    }

    default QueryBuilder<T> sortDesc(String path) {
        return this.sortBy(path, SortDirection.DESC);
    }

    default QueryBuilder<T> sortDesc(String path, NullHandling nullHandling) {
        return this.sortBy(path, SortDirection.DESC, nullHandling);
    }

    QueryBuilder<T> page(int page, int size);

    QueryModel<T> build();
}
