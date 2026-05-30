package io.github.nnhieu.jpaspecbuilder.spring.support;

import io.github.nnhieu.jpaspecbuilder.core.model.FilterCriterion;
import io.github.nnhieu.jpaspecbuilder.core.model.FilterGroup;
import io.github.nnhieu.jpaspecbuilder.core.model.FilterNode;
import io.github.nnhieu.jpaspecbuilder.core.model.JoinSpec;
import io.github.nnhieu.jpaspecbuilder.core.model.LogicalOperator;
import io.github.nnhieu.jpaspecbuilder.core.model.QueryModel;
import io.github.nnhieu.jpaspecbuilder.spring.operator.Operator;
import io.github.nnhieu.jpaspecbuilder.spring.operator.registry.OperatorRegistry;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SpecificationBuilder {

    private final PathResolver pathResolver;
    private final OperatorRegistry operatorRegistry;

    public SpecificationBuilder(PathResolver pathResolver, OperatorRegistry operatorRegistry) {
        this.pathResolver = Objects.requireNonNull(pathResolver);
        this.operatorRegistry = Objects.requireNonNull(operatorRegistry);
    }

    public <T> Specification<T> create(QueryModel<T> model) {
        return (root, query, cb) -> {
            SortPathValidator.validate(model, root);
            Predicate predicate = this.toPredicate(model.getFilter(), root, cb);
            List<Predicate> joinPredicates = model.getJoins().stream()
                    .map(join -> this.joinToPredicate(join, root, cb))
                    .toList();
            Predicate result = this.and(cb, predicate, joinPredicates);
            this.applyDistinctForCollectionJoins(root, query);
            return result;
        };
    }

    private Predicate toPredicate(FilterNode node, From<?, ?> from, CriteriaBuilder cb) {
        if (node instanceof FilterCriterion criterion) {
            return this.criterionToPredicate(criterion, from, cb);
        }
        if (node instanceof FilterGroup group) {
            return this.groupToPredicate(group, from, cb);
        }
        return cb.conjunction();
    }

    private Predicate criterionToPredicate(FilterCriterion criterion, From<?, ?> from, CriteriaBuilder cb) {
        Path<?> path = this.pathResolver.resolve(from, criterion.getPath());
        Operator operator = this.operatorRegistry.find(criterion.getOperator());
        return operator.toPredicate(criterion.getOperator(), path, cb, criterion.getValue());
    }

    private Predicate groupToPredicate(FilterGroup group, From<?, ?> from, CriteriaBuilder cb) {
        List<Predicate> predicates = group.getChildren().stream()
                .map(child -> this.toPredicate(child, from, cb))
                .toList();
        if (predicates.isEmpty()) {
            return cb.conjunction();
        }
        Predicate[] predicateArray = predicates.toArray(Predicate[]::new);
        return group.getOperator() == LogicalOperator.OR ? cb.or(predicateArray) : cb.and(predicateArray);
    }

    private Predicate joinToPredicate(JoinSpec spec, From<?, ?> from, CriteriaBuilder cb) {
        Join<?, ?> join = CriteriaJoinUtils.resolveJoinPath(from, spec.getPath(), spec.getMode().toCriteriaJoinType());
        Predicate predicate = this.toPredicate(spec.getFilter(), join, cb);
        List<Predicate> joinPredicates = spec.getJoins().stream()
                .map(child -> this.joinToPredicate(child, join, cb))
                .toList();
        return this.and(cb, predicate, joinPredicates);
    }

    private Predicate and(CriteriaBuilder cb, Predicate predicate, List<Predicate> predicates) {
        if (predicates.isEmpty()) {
            return predicate;
        }
        List<Predicate> allPredicates = new ArrayList<>(predicates.size() + 1);
        allPredicates.add(predicate);
        allPredicates.addAll(predicates);
        return cb.and(allPredicates.toArray(Predicate[]::new));
    }

    private void applyDistinctForCollectionJoins(Root<?> root, CriteriaQuery<?> query) {
        if (query != null && CriteriaJoinUtils.hasCollectionJoin(root)) {
            query.distinct(true);
        }
    }
}
