package io.github.nnhieu.jpaspecbuilder.spring.support;

import io.github.nnhieu.jpaspecbuilder.core.exception.QueryBuildException;
import io.github.nnhieu.jpaspecbuilder.core.model.QueryModel;
import io.github.nnhieu.jpaspecbuilder.core.model.SortSpec;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.metamodel.Attribute;
import jakarta.persistence.metamodel.ManagedType;
import jakarta.persistence.metamodel.SingularAttribute;

public final class SortPathValidator {

    private SortPathValidator() {
    }

    public static void validate(QueryModel<?> model, Root<?> root) {
        for (SortSpec sort : model.getSorts()) {
            validateSortPath(root.getModel(), sort.getPath());
        }
    }

    public static void validateSortPath(ManagedType<?> rootType, String path) {
        String[] parts = path.split("\\.");
        ManagedType<?> currentType = rootType;
        StringBuilder currentPath = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            if (part.isBlank()) {
                throw new QueryBuildException("Invalid sort path: " + path);
            }
            Attribute<?, ?> attribute = resolveAttribute(currentType, part, path);
            if (!currentPath.isEmpty()) {
                currentPath.append('.');
            }
            currentPath.append(part);
            if (attribute.isCollection()) {
                throw new QueryBuildException("Cannot sort " + rootType.getJavaType().getSimpleName() + " by '" + path + "' because '" + currentPath + "' is a collection association.");
            }
            if (i < parts.length - 1) {
                currentType = resolveManagedType(attribute, path);
            }
        }
    }

    private static Attribute<?, ?> resolveAttribute(ManagedType<?> type, String attributeName, String path) {
        try {
            return type.getAttribute(attributeName);
        } catch (IllegalArgumentException ex) {
            throw new QueryBuildException("Invalid sort path: " + path, ex);
        }
    }

    private static ManagedType<?> resolveManagedType(Attribute<?, ?> attribute, String path) {
        if (attribute instanceof SingularAttribute<?, ?> singularAttribute && singularAttribute.getType() instanceof ManagedType<?> managedType) {
            return managedType;
        }
        throw new QueryBuildException("Invalid sort path: " + path);
    }
}
