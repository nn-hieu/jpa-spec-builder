package io.github.nnhieu.jpaspecbuilder.core.model;

import jakarta.persistence.criteria.JoinType;

public enum JoinMode {
    INNER,
    LEFT,
    RIGHT;

    public JoinType toCriteriaJoinType() {
        return switch (this) {
            case INNER -> JoinType.INNER;
            case LEFT -> JoinType.LEFT;
            case RIGHT -> JoinType.RIGHT;
        };
    }
}
