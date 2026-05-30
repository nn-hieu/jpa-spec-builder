package io.github.nnhieu.jpaspecbuilder.core.query;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Slice;

import java.util.List;
import java.util.Optional;

public interface QueryOperation<T> {

    List<T> findAll();

    Page<T> findPage();

    Slice<T> findSlice();

    Optional<T> findOne();

    long count();

    boolean exists();
}
