package io.github.nnhieu.jpaspecbuilder.core.model;

import org.springframework.data.domain.Sort;

public enum SortDirection {
    ASC,
    DESC;

    public boolean isAscending() {
        return this.equals(ASC);
    }

    public boolean isDescending() {
        return this.equals(DESC);
    }

    public Sort.Direction toSpringDirection() {
        return switch (this) {
            case ASC -> Sort.Direction.ASC;
            case DESC -> Sort.Direction.DESC;
        };
    }
}
