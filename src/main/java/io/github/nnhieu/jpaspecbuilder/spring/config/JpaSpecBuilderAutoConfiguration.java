package io.github.nnhieu.jpaspecbuilder.spring.config;

import io.github.nnhieu.jpaspecbuilder.JpaQueryFactory;
import io.github.nnhieu.jpaspecbuilder.spring.operator.Operator;
import io.github.nnhieu.jpaspecbuilder.spring.operator.registry.DefaultOperatorRegistry;
import io.github.nnhieu.jpaspecbuilder.spring.operator.registry.OperatorRegistry;
import io.github.nnhieu.jpaspecbuilder.spring.support.DefaultPathResolver;
import io.github.nnhieu.jpaspecbuilder.spring.support.PathResolver;
import io.github.nnhieu.jpaspecbuilder.spring.support.SpecificationBuilder;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class JpaSpecBuilderAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public PathResolver jpaPathResolver() {
        return new DefaultPathResolver();
    }

    @Bean
    @ConditionalOnMissingBean
    public OperatorRegistry defaultOperatorRegistry(ObjectProvider<Operator> customHandlers) {
        OperatorRegistry registry = new DefaultOperatorRegistry();
        customHandlers.orderedStream().forEach(registry::register);
        return registry;
    }

    @Bean
    @ConditionalOnMissingBean
    public SpecificationBuilder specificationFactory(PathResolver pathResolver, OperatorRegistry operatorRegistry) {
        return new SpecificationBuilder(pathResolver, operatorRegistry);
    }

    @Bean
    @ConditionalOnMissingBean
    public JpaQueryFactory jpaQueryFactory(SpecificationBuilder specificationBuilder) {
        return new JpaQueryFactory(specificationBuilder);
    }
}
