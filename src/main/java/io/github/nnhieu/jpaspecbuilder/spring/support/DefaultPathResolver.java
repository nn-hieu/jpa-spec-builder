package io.github.nnhieu.jpaspecbuilder.spring.support;

import io.github.nnhieu.jpaspecbuilder.core.exception.QueryBuildException;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;

public class DefaultPathResolver implements PathResolver {

    @Override
    public Path<?> resolve(Root<?> root, String path) {
        return this.resolve((From<?, ?>) root, path);
    }

    @Override
    public Path<?> resolve(From<?, ?> from, String path) {
        if (path == null || path.isBlank()) {
            throw new QueryBuildException("Path must not be blank");
        }
        String[] parts = path.split("\\.");
        Path<?> current = from;
        From<?, ?> currentFrom = from;
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            if (part.isBlank()) {
                throw new QueryBuildException("Invalid path: " + path);
            }
            boolean leaf = i == (parts.length - 1);
            if (leaf) {
                return current.get(part);
            }
            Join<?, ?> join = this.resolveJoin(currentFrom, part);
            current = join;
            currentFrom = join;
        }
        return current;
    }

    private Join<?, ?> resolveJoin(From<?, ?> from, String attributeName) {
        return from.getJoins().stream()
                .filter(join -> this.isSameLeftJoin(join, attributeName))
                .findFirst()
                .orElseGet(() -> from.join(attributeName, JoinType.LEFT));
    }

    private boolean isSameLeftJoin(Join<?, ?> join, String attributeName) {
        return join.getJoinType() == JoinType.LEFT && attributeName.equals(join.getAttribute().getName());
    }
}
