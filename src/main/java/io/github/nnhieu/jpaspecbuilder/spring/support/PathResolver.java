package io.github.nnhieu.jpaspecbuilder.spring.support;

import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;

public interface PathResolver {

    Path<?> resolve(Root<?> root, String path);

    Path<?> resolve(From<?, ?> from, String path);
}
