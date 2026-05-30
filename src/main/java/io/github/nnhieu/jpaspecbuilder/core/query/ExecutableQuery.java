package io.github.nnhieu.jpaspecbuilder.core.query;

import io.github.nnhieu.jpaspecbuilder.core.model.NullHandling;
import io.github.nnhieu.jpaspecbuilder.core.model.SortDirection;
import org.springframework.lang.NonNull;

import java.util.Collection;
import java.util.function.Consumer;

public interface ExecutableQuery<T> extends QueryBuilder<T>, QueryOperation<T> {

    @Override
    ExecutableQuery<T> equals(String path, @NonNull Object value);

    @Override
    ExecutableQuery<T> notEquals(String path, @NonNull Object value);

    @Override
    ExecutableQuery<T> like(String path, String value);

    @Override
    ExecutableQuery<T> ilike(String path, String value);

    @Override
    ExecutableQuery<T> startsWith(String path, String value);

    @Override
    ExecutableQuery<T> endsWith(String path, String value);

    @Override
    ExecutableQuery<T> contains(String path, String value);

    @Override
    <V extends Comparable<? super V>> ExecutableQuery<T> between(String path, V from, V to);

    @Override
    ExecutableQuery<T> in(String path, Collection<?> values);

    @Override
    <V> ExecutableQuery<T> in(String path, V[] values);

    @Override
    ExecutableQuery<T> inIgnoreCase(String path, Collection<String> values);

    @Override
    ExecutableQuery<T> notIn(String path, Collection<?> values);

    @Override
    <V> ExecutableQuery<T> notIn(String path, V[] values);

    @Override
    ExecutableQuery<T> notInIgnoreCase(String path, Collection<String> values);

    @Override
    ExecutableQuery<T> greaterThan(String path, Comparable<?> value);

    @Override
    ExecutableQuery<T> greaterThanOrEqual(String path, Comparable<?> value);

    @Override
    ExecutableQuery<T> lessThan(String path, Comparable<?> value);

    @Override
    ExecutableQuery<T> lessThanOrEqual(String path, Comparable<?> value);

    @Override
    ExecutableQuery<T> isNull(String path);

    @Override
    ExecutableQuery<T> isNotNull(String path);

    @Override
    ExecutableQuery<T> join(String path);

    @Override
    ExecutableQuery<T> join(String path, Consumer<QueryBuilder<T>> group);

    @Override
    ExecutableQuery<T> leftJoin(String path);

    @Override
    ExecutableQuery<T> leftJoin(String path, Consumer<QueryBuilder<T>> group);

    @Override
    ExecutableQuery<T> rightJoin(String path);

    @Override
    ExecutableQuery<T> rightJoin(String path, Consumer<QueryBuilder<T>> group);

    @Override
    ExecutableQuery<T> and(Consumer<QueryBuilder<T>> group);

    @Override
    ExecutableQuery<T> or(Consumer<QueryBuilder<T>> group);

    @Override
    ExecutableQuery<T> sortBy(String path, SortDirection direction);

    @Override
    ExecutableQuery<T> sortBy(String path, SortDirection direction, NullHandling nullHandling);

    @Override
    default ExecutableQuery<T> sortAsc(String path) {
        return this.sortBy(path, SortDirection.ASC);
    }

    @Override
    default ExecutableQuery<T> sortAsc(String path, NullHandling nullHandling) {
        return this.sortBy(path, SortDirection.ASC, nullHandling);
    }

    @Override
    default ExecutableQuery<T> sortDesc(String path) {
        return this.sortBy(path, SortDirection.DESC);
    }

    @Override
    default ExecutableQuery<T> sortDesc(String path, NullHandling nullHandling) {
        return this.sortBy(path, SortDirection.DESC, nullHandling);
    }

    @Override
    ExecutableQuery<T> page(int page, int size);
}
