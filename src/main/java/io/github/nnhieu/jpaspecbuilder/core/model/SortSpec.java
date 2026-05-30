package io.github.nnhieu.jpaspecbuilder.core.model;

import java.util.Objects;

public final class SortSpec {

    private final String path;
    private final SortDirection direction;
    private final NullHandling nullHandling;

    public SortSpec(String path, SortDirection direction, NullHandling nullHandling) {
        if (path == null || path.isBlank()) {
            throw new IllegalArgumentException("Sort path must not be blank");
        }
        this.path = path;
        this.direction = direction == null ? SortDirection.ASC : direction;
        this.nullHandling = nullHandling == null ? NullHandling.NATIVE : nullHandling;
    }

    public static SortSpec asc(String path) {
        return new SortSpec(path, SortDirection.ASC, NullHandling.NATIVE);
    }

    public static SortSpec asc(String path, NullHandling nullHandling) {
        return new SortSpec(path, SortDirection.ASC, nullHandling);
    }

    public static SortSpec desc(String path) {
        return new SortSpec(path, SortDirection.DESC, NullHandling.NATIVE);
    }

    public static SortSpec desc(String path, NullHandling nullHandling) {
        return new SortSpec(path, SortDirection.DESC, nullHandling);
    }

    public String getPath() {
        return this.path;
    }

    public SortDirection getDirection() {
        return this.direction;
    }

    public NullHandling getNullHandling() {
        return this.nullHandling;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SortSpec sortSpec)) {
            return false;
        }
        return Objects.equals(this.path, sortSpec.path)
                && this.direction == sortSpec.direction
                && this.nullHandling == sortSpec.nullHandling;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.path, this.direction, this.nullHandling);
    }

    @Override
    public String toString() {
        return "SortSpec[path=" + this.path + ", direction=" + this.direction + ", nullHandling=" + this.nullHandling + ']';
    }
}
