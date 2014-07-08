package com.github.mwedgwood.service;

import org.skife.jdbi.v2.DBI;

public interface PersistenceService {

    DBI getDbi();

    void initialize();

    void destroy();
}
