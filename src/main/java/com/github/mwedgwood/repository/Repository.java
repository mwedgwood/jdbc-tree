package com.github.mwedgwood.repository;

public interface Repository<T> {

    T findById(Integer id);

    RepositoryResult<T> findAll();

    void save(T entity);

    void delete(T entity);

    void update(T entity);
}
