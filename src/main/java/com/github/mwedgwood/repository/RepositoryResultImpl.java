package com.github.mwedgwood.repository;

import org.skife.jdbi.v2.Query;

import java.util.List;

class RepositoryResultImpl<T> implements RepositoryResult<T> {

    private final Query<T> query;

    RepositoryResultImpl(Query<T> query) {
        this.query = query;
    }

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
        return query.list();
    }

}
