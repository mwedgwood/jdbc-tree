package com.github.mwedgwood.repository;

import java.util.List;

public interface RepositoryResult<T> {

    RepositoryResult<T> limit(int page, int perPage);

    RepositoryResult<T> orderBy(Order... orders);

    List<T> list();
}
