package io.github.nnhieu.jpaspecbuilder;

import io.github.nnhieu.jpaspecbuilder.core.query.ExecutableQuery;
import io.github.nnhieu.jpaspecbuilder.core.query.JpaExecutableQuery;
import io.github.nnhieu.jpaspecbuilder.core.query.QueryBuilder;
import io.github.nnhieu.jpaspecbuilder.spring.support.SpecificationBuilder;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Objects;

public class JpaQueryFactory {

    private final SpecificationBuilder specificationBuilder;

    public JpaQueryFactory(SpecificationBuilder specificationBuilder) {
        this.specificationBuilder = Objects.requireNonNull(specificationBuilder);
    }

    public <T> QueryBuilder<T> builder(Class<T> entityClass) {
        return QueryBuilder.builder(entityClass);
    }

    public <T> ExecutableQuery<T> query(Class<T> entityClass, JpaSpecificationExecutor<T> repository) {
        return new JpaExecutableQuery<>(entityClass, repository, this.specificationBuilder);
    }
}
