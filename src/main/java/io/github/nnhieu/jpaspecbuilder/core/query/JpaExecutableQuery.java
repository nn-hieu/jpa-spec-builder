package io.github.nnhieu.jpaspecbuilder.core.query;

import io.github.nnhieu.jpaspecbuilder.core.exception.QueryBuildException;
import io.github.nnhieu.jpaspecbuilder.core.model.FilterCriterion;
import io.github.nnhieu.jpaspecbuilder.core.model.FilterGroup;
import io.github.nnhieu.jpaspecbuilder.core.model.FilterNode;
import io.github.nnhieu.jpaspecbuilder.core.model.FilterOperator;
import io.github.nnhieu.jpaspecbuilder.core.model.JoinMode;
import io.github.nnhieu.jpaspecbuilder.core.model.JoinSpec;
import io.github.nnhieu.jpaspecbuilder.core.model.LogicalOperator;
import io.github.nnhieu.jpaspecbuilder.core.model.NullHandling;
import io.github.nnhieu.jpaspecbuilder.core.model.PageSpec;
import io.github.nnhieu.jpaspecbuilder.core.model.QueryModel;
import io.github.nnhieu.jpaspecbuilder.core.model.SortDirection;
import io.github.nnhieu.jpaspecbuilder.core.model.SortSpec;
import io.github.nnhieu.jpaspecbuilder.spring.support.SortMapper;
import io.github.nnhieu.jpaspecbuilder.spring.support.SpecificationBuilder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.lang.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

public class JpaExecutableQuery<T> implements ExecutableQuery<T> {

    private static final PageSpec DEFAULT_PAGE = new PageSpec(0, 10);
    private static final LogicalOperator DEFAULT_FILTER_GROUP_OPERATOR = LogicalOperator.AND;

    private final Class<T> entityClass;
    private final JpaSpecificationExecutor<T> repository;
    private final SpecificationBuilder specificationBuilder;
    private final LogicalOperator filterGroupOperator;
    private final List<FilterNode> filters = new ArrayList<>();
    private final List<JoinSpec> joins = new ArrayList<>();
    private final List<SortSpec> sorts = new ArrayList<>();

    private PageSpec page;

    public JpaExecutableQuery(Class<T> entityClass) {
        this(
                entityClass,
                null,
                null,
                DEFAULT_FILTER_GROUP_OPERATOR
        );
    }

    public JpaExecutableQuery(Class<T> entityClass, JpaSpecificationExecutor<T> repository, SpecificationBuilder specificationBuilder) {
        this(
                entityClass,
                Objects.requireNonNull(repository),
                Objects.requireNonNull(specificationBuilder),
                DEFAULT_FILTER_GROUP_OPERATOR
        );
    }

    private JpaExecutableQuery(Class<T> entityClass, JpaSpecificationExecutor<T> repository, SpecificationBuilder specificationBuilder, LogicalOperator filterGroupOperator) {
        this.entityClass = Objects.requireNonNull(entityClass);
        this.repository = repository;
        this.specificationBuilder = specificationBuilder;
        this.filterGroupOperator = Objects.requireNonNull(filterGroupOperator);
    }

    @Override
    public JpaExecutableQuery<T> equals(String path, @NonNull Object value) {
        FilterCriterion criterion = new FilterCriterion(path, FilterOperator.EQUALS, value);
        this.filters.add(criterion);
        return this;
    }

    @Override
    public JpaExecutableQuery<T> notEquals(String path, @NonNull Object value) {
        FilterCriterion criterion = new FilterCriterion(path, FilterOperator.NOT_EQUALS, value);
        this.filters.add(criterion);
        return this;
    }

    @Override
    public JpaExecutableQuery<T> like(String path, String value) {
        FilterCriterion criterion = new FilterCriterion(path, FilterOperator.LIKE, value);
        this.filters.add(criterion);
        return this;
    }

    @Override
    public JpaExecutableQuery<T> ilike(String path, String value) {
        FilterCriterion criterion = new FilterCriterion(path, FilterOperator.ILIKE, value);
        this.filters.add(criterion);
        return this;
    }

    @Override
    public JpaExecutableQuery<T> startsWith(String path, String value) {
        FilterCriterion criterion = new FilterCriterion(path, FilterOperator.STARTS_WITH, value);
        this.filters.add(criterion);
        return this;
    }

    @Override
    public JpaExecutableQuery<T> endsWith(String path, String value) {
        FilterCriterion criterion = new FilterCriterion(path, FilterOperator.ENDS_WITH, value);
        this.filters.add(criterion);
        return this;
    }

    @Override
    public JpaExecutableQuery<T> contains(String path, String value) {
        FilterCriterion criterion = new FilterCriterion(path, FilterOperator.CONTAINS, value);
        this.filters.add(criterion);
        return this;
    }

    @Override
    public <V extends Comparable<? super V>> JpaExecutableQuery<T> between(String path, V from, V to) {
        List<Object> values = List.of(from, to);
        FilterCriterion criterion = new FilterCriterion(path, FilterOperator.BETWEEN, values);
        this.filters.add(criterion);
        return this;
    }

    @Override
    public JpaExecutableQuery<T> in(String path, Collection<?> values) {
        FilterCriterion criterion = new FilterCriterion(path, FilterOperator.IN, values);
        this.filters.add(criterion);
        return this;
    }

    @Override
    public <V> ExecutableQuery<T> in(String path, V[] values) {
        return this.in(path, List.of(values));
    }

    @Override
    public JpaExecutableQuery<T> inIgnoreCase(String path, Collection<String> values) {
        FilterCriterion criterion = new FilterCriterion(path, FilterOperator.IN_IGNORE_CASE, values);
        this.filters.add(criterion);
        return this;
    }

    @Override
    public JpaExecutableQuery<T> notIn(String path, Collection<?> values) {
        FilterCriterion criterion = new FilterCriterion(path, FilterOperator.NOT_IN, values);
        this.filters.add(criterion);
        return this;
    }

    @Override
    public <V> ExecutableQuery<T> notIn(String path, V[] values) {
        return this.notIn(path, List.of(values));
    }

    @Override
    public JpaExecutableQuery<T> notInIgnoreCase(String path, Collection<String> values) {
        FilterCriterion criterion = new FilterCriterion(path, FilterOperator.NOT_IN_IGNORE_CASE, values);
        this.filters.add(criterion);
        return this;
    }

    @Override
    public JpaExecutableQuery<T> greaterThan(String path, Comparable<?> value) {
        FilterCriterion criterion = new FilterCriterion(path, FilterOperator.GREATER_THAN, value);
        this.filters.add(criterion);
        return this;
    }

    @Override
    public JpaExecutableQuery<T> greaterThanOrEqual(String path, Comparable<?> value) {
        FilterCriterion criterion = new FilterCriterion(path, FilterOperator.GREATER_THAN_EQUAL, value);
        this.filters.add(criterion);
        return this;
    }

    @Override
    public JpaExecutableQuery<T> lessThan(String path, Comparable<?> value) {
        FilterCriterion criterion = new FilterCriterion(path, FilterOperator.LESS_THAN, value);
        this.filters.add(criterion);
        return this;
    }

    @Override
    public JpaExecutableQuery<T> lessThanOrEqual(String path, Comparable<?> value) {
        FilterCriterion criterion = new FilterCriterion(path, FilterOperator.LESS_THAN_EQUAL, value);
        this.filters.add(criterion);
        return this;
    }

    @Override
    public JpaExecutableQuery<T> isNull(String path) {
        FilterCriterion criterion = new FilterCriterion(path, FilterOperator.IS_NULL, null);
        this.filters.add(criterion);
        return this;
    }

    @Override
    public JpaExecutableQuery<T> isNotNull(String path) {
        FilterCriterion criterion = new FilterCriterion(path, FilterOperator.IS_NOT_NULL, null);
        this.filters.add(criterion);
        return this;
    }

    @Override
    public JpaExecutableQuery<T> join(String path) {
        return this.addJoin(path, JoinMode.INNER);
    }

    @Override
    public JpaExecutableQuery<T> join(String path, Consumer<QueryBuilder<T>> group) {
        return this.addJoin(path, JoinMode.INNER, group);
    }

    @Override
    public JpaExecutableQuery<T> leftJoin(String path) {
        return this.addJoin(path, JoinMode.LEFT);
    }

    @Override
    public JpaExecutableQuery<T> leftJoin(String path, Consumer<QueryBuilder<T>> group) {
        return this.addJoin(path, JoinMode.LEFT, group);
    }

    @Override
    public JpaExecutableQuery<T> rightJoin(String path) {
        return this.addJoin(path, JoinMode.RIGHT);
    }

    @Override
    public JpaExecutableQuery<T> rightJoin(String path, Consumer<QueryBuilder<T>> group) {
        return this.addJoin(path, JoinMode.RIGHT, group);
    }

    @Override
    public JpaExecutableQuery<T> and(Consumer<QueryBuilder<T>> group) {
        return this.addGroup(LogicalOperator.AND, group);
    }

    @Override
    public JpaExecutableQuery<T> or(Consumer<QueryBuilder<T>> group) {
        return this.addGroup(LogicalOperator.OR, group);
    }

    @Override
    public JpaExecutableQuery<T> sortBy(String path, SortDirection direction) {
        return this.sortBy(path, direction, NullHandling.NATIVE);
    }

    @Override
    public JpaExecutableQuery<T> sortBy(String path, SortDirection direction, NullHandling nullHandling) {
        this.sorts.add(new SortSpec(path, direction, nullHandling));
        return this;
    }

    @Override
    public JpaExecutableQuery<T> page(int page, int size) {
        this.page = new PageSpec(page, size);
        return this;
    }

    @Override
    public QueryModel<T> build() {
        return new QueryModel<>(this.entityClass, new FilterGroup(this.filterGroupOperator, this.filters), this.joins, this.sorts, this.page);
    }

    @Override
    public List<T> findAll() {
        JpaSpecificationExecutor<T> executor = this.repository();
        QueryModel<T> model = this.build();
        return executor.findAll(this.specification(model), SortMapper.toSort(model.getSorts()));
    }

    @Override
    public Page<T> findPage() {
        JpaSpecificationExecutor<T> executor = this.repository();
        QueryModel<T> model = this.build();
        PageSpec pageSpec = model.getPage() == null ? DEFAULT_PAGE : model.getPage();
        Pageable pageable = PageRequest.of(
                pageSpec.getPage(),
                pageSpec.getSize(),
                SortMapper.toSort(model.getSorts())
        );
        return executor.findAll(this.specification(model), pageable);
    }

    @Override
    public Slice<T> findSlice() {
        JpaSpecificationExecutor<T> executor = this.repository();
        QueryModel<T> model = this.build();
        PageSpec pageSpec = model.getPage() == null ? DEFAULT_PAGE : model.getPage();
        Pageable pageable = PageRequest.of(
                pageSpec.getPage(),
                pageSpec.getSize(),
                SortMapper.toSort(model.getSorts())
        );
        int requestedRows = (int) pageable.getOffset() + pageable.getPageSize() + 1;
        List<T> results = executor.findBy(
                this.specification(model),
                query -> query.sortBy(pageable.getSort()).limit(requestedRows).all()
        );
        boolean hasNext = results.size() > pageable.getOffset() + pageable.getPageSize();
        int fromIndex = (int) Math.min(pageable.getOffset(), results.size());
        int toIndex = Math.min(fromIndex + pageable.getPageSize(), results.size());
        List<T> content = results.subList(fromIndex, toIndex);
        return new SliceImpl<>(content, pageable, hasNext);
    }

    @Override
    public Optional<T> findOne() {
        JpaSpecificationExecutor<T> executor = this.repository();
        return executor.findOne(this.specification(this.build()));
    }

    @Override
    public long count() {
        JpaSpecificationExecutor<T> executor = this.repository();
        return executor.count(this.specification(this.build()));
    }

    @Override
    public boolean exists() {
        JpaSpecificationExecutor<T> executor = this.repository();
        return executor.exists(this.specification(this.build()));
    }

    private JpaExecutableQuery<T> addGroup(LogicalOperator operator, Consumer<QueryBuilder<T>> group) {
        Objects.requireNonNull(group);
        JpaExecutableQuery<T> child = new JpaExecutableQuery<>(this.entityClass, this.repository, this.specificationBuilder, operator);
        group.accept(child);
        FilterGroup filterGroup = (FilterGroup) child.build().getFilter();
        if (!filterGroup.getChildren().isEmpty()) {
            this.filters.add(filterGroup);
        }
        return this;
    }

    private JpaExecutableQuery<T> addJoin(String path, JoinMode mode, Consumer<QueryBuilder<T>> group) {
        Objects.requireNonNull(path);
        Objects.requireNonNull(group);
        JpaExecutableQuery<T> child = new JpaExecutableQuery<>(this.entityClass, this.repository, this.specificationBuilder, LogicalOperator.AND);
        group.accept(child);
        QueryModel<T> model = child.build();
        this.joins.add(new JoinSpec(path, mode, model.getFilter(), model.getJoins()));
        return this;
    }

    private JpaExecutableQuery<T> addJoin(String path, JoinMode mode) {
        Objects.requireNonNull(path);
        this.joins.add(new JoinSpec(path, mode, FilterGroup.emptyAnd(), List.of()));
        return this;
    }

    private Specification<T> specification(QueryModel<T> model) {
        if (this.specificationBuilder == null) {
            throw new QueryBuildException("Cannot build specification because no SpecificationBuilder was provided");
        }
        return this.specificationBuilder.create(model);
    }

    private JpaSpecificationExecutor<T> repository() {
        if (this.repository == null) {
            throw new QueryBuildException("This query is not executable because no JpaSpecificationExecutor was provided");
        }
        return this.repository;
    }
}
