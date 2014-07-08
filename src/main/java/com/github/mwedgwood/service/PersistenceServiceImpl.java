package com.github.mwedgwood.service;

import org.h2.jdbcx.JdbcDataSource;
import org.skife.jdbi.v2.DBI;


public class PersistenceServiceImpl implements PersistenceService {

    private DBI dbi;

    private PersistenceServiceImpl() {
        initialize();
    }

    private static class SingletonHolder {
        private static final PersistenceServiceImpl INSTANCE = new PersistenceServiceImpl();
    }

    public static PersistenceServiceImpl getInstance() {
        return SingletonHolder.INSTANCE;
    }

    @Override
    public DBI getDbi() {
        return dbi;
    }

    @Override
    public void initialize() {
        JdbcDataSource jdbcDataSource = new JdbcDataSource();
        jdbcDataSource.setURL("jdbc:h2:mem:test;MODE=PostgreSQL");
        dbi = new DBI(jdbcDataSource);
    }

    @Override
    public void destroy() {
        dbi = null;
    }

}
