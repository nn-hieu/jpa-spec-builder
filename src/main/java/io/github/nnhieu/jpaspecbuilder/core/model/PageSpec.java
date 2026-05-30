package io.github.nnhieu.jpaspecbuilder.core.model;

import java.util.Objects;

public final class PageSpec {

    private final int page;
    private final int size;

    public PageSpec(int page, int size) {
        if (page < 0) {
            throw new IllegalArgumentException("Page index must not be negative");
        }
        if (size < 1) {
            throw new IllegalArgumentException("Page size must be greater than zero");
        }
        this.page = page;
        this.size = size;
    }

    public int getPage() {
        return this.page;
    }

    public int getSize() {
        return this.size;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PageSpec pageSpec)) {
            return false;
        }
        return this.page == pageSpec.page && this.size == pageSpec.size;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.page, this.size);
    }

    @Override
    public String toString() {
        return "PageSpec[page=" + this.page + ", size=" + this.size + ']';
    }
}
