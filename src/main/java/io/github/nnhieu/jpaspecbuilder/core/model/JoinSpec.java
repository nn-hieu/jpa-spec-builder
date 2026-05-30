package io.github.nnhieu.jpaspecbuilder.core.model;

import java.util.List;
import java.util.Objects;

public final class JoinSpec {

    private final String path;
    private final JoinMode mode;
    private final FilterNode filter;
    private final List<JoinSpec> joins;

    public JoinSpec(String path, JoinMode mode, FilterNode filter, List<JoinSpec> joins) {
        if (path == null || path.isBlank()) {
            throw new IllegalArgumentException("Join path must not be blank");
        }
        this.path = path;
        this.mode = Objects.requireNonNull(mode);
        this.filter = filter == null ? FilterGroup.emptyAnd() : filter;
        this.joins = joins == null ? List.of() : List.copyOf(joins);
    }

    public String getPath() {
        return this.path;
    }

    public JoinMode getMode() {
        return this.mode;
    }

    public FilterNode getFilter() {
        return this.filter;
    }

    public List<JoinSpec> getJoins() {
        return this.joins;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof JoinSpec that)) {
            return false;
        }
        return Objects.equals(this.path, that.path)
                && this.mode == that.mode
                && Objects.equals(this.filter, that.filter)
                && Objects.equals(this.joins, that.joins);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.path, this.mode, this.filter, this.joins);
    }

    @Override
    public String toString() {
        return "JoinSpec[" + "path=" + this.path + ", mode=" + this.mode + ", filter=" + this.filter + ", joins=" + this.joins + ']';
    }
}
