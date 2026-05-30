package io.github.nnhieu.jpaspecbuilder.core.model;

import java.util.List;
import java.util.Objects;

public final class FilterGroup implements FilterNode {

    private final LogicalOperator operator;
    private final List<FilterNode> children;

    public FilterGroup(LogicalOperator operator, List<FilterNode> children) {
        this.operator = Objects.requireNonNull(operator);
        this.children = children == null ? List.of() : List.copyOf(children);
    }

    public static FilterGroup emptyAnd() {
        return new FilterGroup(LogicalOperator.AND, List.of());
    }

    public LogicalOperator getOperator() {
        return this.operator;
    }

    public List<FilterNode> getChildren() {
        return this.children;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof FilterGroup that)) {
            return false;
        }
        return this.operator == that.operator && Objects.equals(this.children, that.children);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.operator, this.children);
    }

    @Override
    public String toString() {
        return "FilterGroup[logicalOperator=" + this.operator + ", children=" + this.children + ']';
    }
}
