package io.github.nnhieu.jpaspecbuilder.core.query;

import io.github.nnhieu.jpaspecbuilder.core.model.PageSpec;
import io.github.nnhieu.jpaspecbuilder.core.model.QueryModel;
import io.github.nnhieu.jpaspecbuilder.spring.support.PathResolver;
import io.github.nnhieu.jpaspecbuilder.spring.support.SortMapper;
import io.github.nnhieu.jpaspecbuilder.spring.support.SpecificationBuilder;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NonUniqueResultException;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class QueryModelExecutor {

    private static final PageSpec DEFAULT_PAGE = new PageSpec(0, 20);

    private final EntityManager entityManager;
    private final SpecificationBuilder specificationBuilder;
    private final PathResolver pathResolver;

    public QueryModelExecutor(EntityManager entityManager, SpecificationBuilder specificationBuilder, PathResolver pathResolver) {
        this.entityManager = Objects.requireNonNull(entityManager);
        this.specificationBuilder = Objects.requireNonNull(specificationBuilder);
        this.pathResolver = Objects.requireNonNull(pathResolver);
    }

    public <T> QueryOperation<T> executable(QueryModel<T> model) {
        return new QueryModelOperation<>(this, model);
    }

    public <T> List<T> findAll(QueryModel<T> model) {
        Objects.requireNonNull(model);
        return this.createTypedQuery(model).getResultList();
    }

    public <T> Page<T> findPage(QueryModel<T> model) {
        Objects.requireNonNull(model);
        PageSpec pageSpec = model.getPage() == null ? DEFAULT_PAGE : model.getPage();
        Pageable pageable = PageRequest.of(pageSpec.getPage(), pageSpec.getSize());
        TypedQuery<T> typedQuery = this.createTypedQuery(model)
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize());
        return new PageImpl<>(typedQuery.getResultList(), pageable, this.count(model));
    }

    public <T> Slice<T> findSlice(QueryModel<T> model) {
        Objects.requireNonNull(model);
        PageSpec pageSpec = model.getPage() == null ? DEFAULT_PAGE : model.getPage();
        Pageable pageable = PageRequest.of(pageSpec.getPage(), pageSpec.getSize());
        List<T> results = this.createTypedQuery(model)
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize() + 1)
                .getResultList();
        boolean hasNext = results.size() > pageable.getPageSize();
        List<T> content = hasNext ? results.subList(0, pageable.getPageSize()) : results;
        return new SliceImpl<>(content, pageable, hasNext);
    }

    public <T> Optional<T> findOne(QueryModel<T> model) {
        Objects.requireNonNull(model);
        List<T> results = this.createTypedQuery(model)
                .setMaxResults(2)
                .getResultList();
        if (results.size() > 1) {
            throw new NonUniqueResultException("Query returned more than one result");
        }
        return results.stream().findFirst();
    }

    public <T> long count(QueryModel<T> model) {
        Objects.requireNonNull(model);
        CriteriaBuilder cb = this.entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<T> root = query.from(model.getEntityClass());
        this.applyPredicate(model, query, root, cb);
        query.select(query.isDistinct() ? cb.countDistinct(root) : cb.count(root));
        return this.entityManager.createQuery(query).getSingleResult();
    }

    public <T> boolean exists(QueryModel<T> model) {
        Objects.requireNonNull(model);
        return !this.createTypedQuery(model)
                .setMaxResults(1)
                .getResultList()
                .isEmpty();
    }

    private <T> TypedQuery<T> createTypedQuery(QueryModel<T> model) {
        CriteriaBuilder cb = this.entityManager.getCriteriaBuilder();
        CriteriaQuery<T> query = cb.createQuery(model.getEntityClass());
        Root<T> root = query.from(model.getEntityClass());
        this.applyPredicate(model, query, root, cb);
        this.applySorting(model, query, root, cb);
        return this.entityManager.createQuery(query);
    }

    private <T> void applyPredicate(QueryModel<T> model, CriteriaQuery<?> query, Root<T> root, CriteriaBuilder cb) {
        Specification<T> specification = this.specificationBuilder.create(model);
        Predicate predicate = specification.toPredicate(root, query, cb);
        if (predicate != null) {
            query.where(predicate);
        }
    }

    private <T> void applySorting(QueryModel<T> model, CriteriaQuery<T> query, Root<T> root, CriteriaBuilder cb) {
        List<Order> orders = SortMapper.toCriteriaOrders(model.getSorts(), root, cb, this.pathResolver);
        if (!orders.isEmpty()) {
            query.orderBy(orders);
        }
    }

    private static final class QueryModelOperation<T> implements QueryOperation<T> {

        private final QueryModelExecutor executor;
        private final QueryModel<T> model;

        private QueryModelOperation(QueryModelExecutor executor, QueryModel<T> model) {
            this.executor = executor;
            this.model = Objects.requireNonNull(model);
        }

        @Override
        public List<T> findAll() {
            return this.executor.findAll(this.model);
        }

        @Override
        public Page<T> findPage() {
            return this.executor.findPage(this.model);
        }

        @Override
        public Slice<T> findSlice() {
            return this.executor.findSlice(this.model);
        }

        @Override
        public Optional<T> findOne() {
            return this.executor.findOne(this.model);
        }

        @Override
        public long count() {
            return this.executor.count(this.model);
        }

        @Override
        public boolean exists() {
            return this.executor.exists(this.model);
        }
    }
}
