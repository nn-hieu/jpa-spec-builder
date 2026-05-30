package io.github.nnhieu.jpaspecbuilder.spring.operator.registry;

import io.github.nnhieu.jpaspecbuilder.core.exception.QueryBuildException;
import io.github.nnhieu.jpaspecbuilder.core.model.FilterOperator;
import io.github.nnhieu.jpaspecbuilder.spring.operator.Operator;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.util.ClassUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DefaultOperatorRegistry implements OperatorRegistry {
    private final List<Operator> operators = new ArrayList<>();

    public DefaultOperatorRegistry() {
        this.registerBuiltInOperators();
    }

    @Override
    public void register(Operator operator) {
        this.operators.add(Objects.requireNonNull(operator));
    }

    @Override
    public Operator find(FilterOperator filterOperator) {
        Objects.requireNonNull(filterOperator);
        return this.operators.stream()
                .filter(operator -> operator.supports(filterOperator))
                .findFirst()
                .orElseThrow(() -> new QueryBuildException("Unsupported filter operator: " + filterOperator));
    }

    private void registerBuiltInOperators() {
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AssignableTypeFilter(Operator.class));
        scanner.findCandidateComponents(OPERATOR_PACKAGE)
                .stream()
                .map(this::createOperator)
                .forEach(this::register);
    }

    private Operator createOperator(BeanDefinition candidate) {
        String beanClassName = candidate.getBeanClassName();
        if (beanClassName == null) {
            throw new QueryBuildException("Operator candidate does not define a bean class name");
        }
        try {
            Class<?> type = ClassUtils.forName(beanClassName, DefaultOperatorRegistry.class.getClassLoader());
            return (Operator) type.getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException ex) {
            throw new QueryBuildException("Failed to create operator: " + beanClassName, ex);
        }
    }
}
