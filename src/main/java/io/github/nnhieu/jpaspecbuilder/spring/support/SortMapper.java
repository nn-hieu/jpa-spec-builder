package io.github.nnhieu.jpaspecbuilder.spring.support;

import io.github.nnhieu.jpaspecbuilder.core.model.SortDirection;
import io.github.nnhieu.jpaspecbuilder.core.model.SortSpec;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Objects;

public final class SortMapper {

    private SortMapper() {
    }

    public static Sort toSort(List<SortSpec> specs) {
        if (specs == null || specs.isEmpty()) {
            return Sort.unsorted();
        }
        List<Sort.Order> orders = specs.stream()
                .map(SortMapper::toSortOrder)
                .toList();
        return Sort.by(orders);
    }

    public static Sort.Order toSortOrder(SortSpec spec) {
        Sort.Direction direction = spec.getDirection().toSpringDirection();
        Sort.Order order = new Sort.Order(direction, spec.getPath());
        return switch (spec.getNullHandling()) {
            case NULLS_FIRST -> order.nullsFirst();
            case NULLS_LAST -> order.nullsLast();
            case NATIVE -> order;
        };
    }

    public static List<Order> toCriteriaOrders(List<SortSpec> specs, Root<?> root, CriteriaBuilder cb, PathResolver pathResolver) {
        if (specs == null || specs.isEmpty()) {
            return List.of();
        }
        Objects.requireNonNull(root);
        Objects.requireNonNull(cb);
        Objects.requireNonNull(pathResolver);
        return specs.stream()
                .map(sort -> toCriteriaOrders(sort, root, cb, pathResolver))
                .flatMap(List::stream)
                .toList();
    }

    public static Order toCriteriaOrder(SortSpec sort, Root<?> root, CriteriaBuilder cb, PathResolver pathResolver) {
        return toDirectionalCriteriaOrder(sort, pathResolver.resolve(root, sort.getPath()), cb);
    }

    private static List<Order> toCriteriaOrders(SortSpec sort, Root<?> root, CriteriaBuilder cb, PathResolver pathResolver) {
        Path<?> path = pathResolver.resolve(root, sort.getPath());
        return switch (sort.getNullHandling()) {
            case NULLS_FIRST -> List.of(toNullPrecedenceOrder(path, cb, 0, 1), toDirectionalCriteriaOrder(sort, path, cb));
            case NULLS_LAST -> List.of(toNullPrecedenceOrder(path, cb, 1, 0), toDirectionalCriteriaOrder(sort, path, cb));
            case NATIVE -> List.of(toDirectionalCriteriaOrder(sort, path, cb));
        };
    }

    private static Order toDirectionalCriteriaOrder(SortSpec sort, Path<?> path, CriteriaBuilder cb) {
        return sort.getDirection() == SortDirection.DESC ? cb.desc(path) : cb.asc(path);
    }

    private static Order toNullPrecedenceOrder(Path<?> path, CriteriaBuilder cb, int nullValue, int nonNullValue) {
        return cb.asc(cb.selectCase().when(cb.isNull(path), nullValue).otherwise(nonNullValue));
    }
}
