package io.github.nnhieu.jpaspecbuilder.core.model;

import java.util.Objects;

public final class FilterCriterion implements FilterNode {

    private final String path;
    private final FilterOperator operator;
    private final Object value;

    public FilterCriterion(String path, FilterOperator operator, Object value) {
        if (path == null || path.isBlank()) {
            throw new IllegalArgumentException("Filter path must not be blank");
        }
        this.path = path;
        this.operator = Objects.requireNonNull(operator);
        this.value = value;
    }

    public String getPath() {
        return this.path;
    }

    public FilterOperator getOperator() {
        return this.operator;
    }

    public Object getValue() {
        return this.value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof FilterCriterion that)) {
            return false;
        }
        return Objects.equals(this.path, that.path)
                && this.operator == that.operator
                && Objects.equals(this.value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.path, this.operator, this.value);
    }

    @Override
    public String toString() {
        return "FilterCriterion[" + "path=" + this.path + ", operator=" + this.operator + ", value=" + this.value + ']';
    }
}
