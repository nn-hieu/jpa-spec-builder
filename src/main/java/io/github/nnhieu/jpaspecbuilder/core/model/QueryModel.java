package io.github.nnhieu.jpaspecbuilder.core.model;

import java.util.List;
import java.util.Objects;

public final class QueryModel<T> {

    private final Class<T> entityClass;
    private final FilterNode filter;
    private final List<JoinSpec> joins;
    private final List<SortSpec> sorts;
    private final PageSpec page;

    public QueryModel(Class<T> entityClass, FilterNode filter, List<SortSpec> sorts, PageSpec page) {
        this(entityClass, filter, List.of(), sorts, page);
    }

    public QueryModel(Class<T> entityClass, FilterNode filter, List<JoinSpec> joins, List<SortSpec> sorts, PageSpec page) {
        this.entityClass = Objects.requireNonNull(entityClass);
        this.filter = filter == null ? FilterGroup.emptyAnd() : filter;
        this.joins = joins == null ? List.of() : List.copyOf(joins);
        this.sorts = sorts == null ? List.of() : List.copyOf(sorts);
        this.page = page;
    }

    public Class<T> getEntityClass() {
        return this.entityClass;
    }

    public FilterNode getFilter() {
        return this.filter;
    }

    public List<JoinSpec> getJoins() {
        return this.joins;
    }

    public List<SortSpec> getSorts() {
        return this.sorts;
    }

    public PageSpec getPage() {
        return this.page;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof QueryModel<?> that)) {
            return false;
        }
        return Objects.equals(this.entityClass, that.entityClass)
                && Objects.equals(this.filter, that.filter)
                && Objects.equals(this.joins, that.joins)
                && Objects.equals(this.sorts, that.sorts)
                && Objects.equals(this.page, that.page);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.entityClass, this.filter, this.joins, this.sorts, this.page);
    }

    @Override
    public String toString() {
        return "QueryModel[" + "entityClass=" + this.entityClass + ", filter=" + this.filter + ", joins=" + this.joins + ", sorts=" + this.sorts + ", page=" + this.page + ']';
    }
}
