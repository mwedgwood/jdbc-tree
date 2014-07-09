package com.github.mwedgwood.service;

import org.h2.jdbcx.JdbcDataSource;
import org.postgresql.ds.PGSimpleDataSource;
import org.skife.jdbi.v2.DBI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;


public class PersistenceServiceImpl implements PersistenceService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PersistenceServiceImpl.class);

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
        dbi = new DBI(getDataSource(false));
    }

    @Override
    public void destroy() {
        dbi = null;
    }

    private DataSource getDataSource(boolean useInMemory) {
        if (useInMemory) {
            LOGGER.info("Using in memory database");
            JdbcDataSource jdbcDataSource = new JdbcDataSource();
            jdbcDataSource.setURL("jdbc:h2:mem:test;MODE=PostgreSQL");
            return jdbcDataSource;
        }
        PGSimpleDataSource dataSource = new PGSimpleDataSource();
        dataSource.setServerName("localhost");
        dataSource.setPortNumber(15432);
        dataSource.setDatabaseName("test");
        dataSource.setUser("postgres");
        dataSource.setPassword("postgres");
        return dataSource;
    }

}
