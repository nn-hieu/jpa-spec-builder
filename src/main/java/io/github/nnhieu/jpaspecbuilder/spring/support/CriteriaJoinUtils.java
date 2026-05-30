package io.github.nnhieu.jpaspecbuilder.spring.support;

import io.github.nnhieu.jpaspecbuilder.core.exception.QueryBuildException;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.metamodel.Attribute;

public final class CriteriaJoinUtils {

    private CriteriaJoinUtils() {
    }

    public static Join<?, ?> resolveJoinPath(From<?, ?> from, String path, JoinType type) {
        String[] parts = path.split("\\.");
        From<?, ?> currentFrom = from;
        Join<?, ?> join = null;
        for (String part : parts) {
            if (part.isBlank()) {
                throw new QueryBuildException("Invalid join path: " + path);
            }
            join = resolveJoin(currentFrom, part, type);
            currentFrom = join;
        }
        return join;
    }

    public static boolean hasCollectionJoin(From<?, ?> from) {
        return from.getJoins().stream()
                .anyMatch(join -> isCollectionJoin(join) || hasCollectionJoin(join));
    }

    public static Join<?, ?> resolveJoin(From<?, ?> from, String attributeName, JoinType type) {
        return from.getJoins().stream()
                .filter(join -> isSameJoin(join, attributeName, type))
                .findFirst()
                .orElseGet(() -> from.join(attributeName, type));
    }

    public static boolean isSameJoin(Join<?, ?> join, String attributeName, JoinType type) {
        return join.getJoinType() == type && attributeName.equals(join.getAttribute().getName());
    }

    public static boolean isCollectionJoin(Join<?, ?> join) {
        Attribute<?, ?> attribute = join.getAttribute();
        return attribute != null && attribute.isCollection();
    }
}
