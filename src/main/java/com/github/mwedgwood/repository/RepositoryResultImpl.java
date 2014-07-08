package com.github.mwedgwood.repository;

import java.util.List;

class RepositoryResultImpl<T> implements RepositoryResult<T> {

    @Override
    public RepositoryResult<T> limit(int page, int perPage) {
        return this;
    }

    @Override
    public RepositoryResult<T> orderBy(Order... orders) {
        return this;
    }

    @Override
    public List<T> list() {
        return null;
    }

}
