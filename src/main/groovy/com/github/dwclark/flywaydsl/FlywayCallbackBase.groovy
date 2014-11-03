package com.github.dwclark.flywaydsl;

import org.flywaydb.core.api.callback.FlywayCallback;
import org.flywaydb.core.api.MigrationInfo;
import java.sql.Connection;

public class FlywayCallbackBase implements FlywayCallback {
    
    final ApplicationConfig config;

    public FlywayCallbackBase(ApplicationConfig config) {
        this.config = config;
    }

    public void beforeClean(Connection connection) {}
    public void afterClean(Connection connection) {}
    public void beforeMigrate(Connection connection) {}
    public void afterMigrate(Connection connection) {}
    public void beforeEachMigrate(Connection connection, MigrationInfo info) {}
    public void afterEachMigrate(Connection connection, MigrationInfo info) {}
    public void beforeValidate(Connection connection) {}
    public void afterValidate(Connection connection) {}
    public void beforeInit(Connection connection) {}
    public void afterInit(Connection connection) {}
    public void beforeRepair(Connection connection) {}
    public void afterRepair(Connection connection) {}
    public void beforeInfo(Connection connection) {}
    public void afterInfo(Connection connection) {}
}