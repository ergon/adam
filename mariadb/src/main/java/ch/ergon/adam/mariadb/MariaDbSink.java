package ch.ergon.adam.mariadb;

import ch.ergon.adam.jooq.JooqSink;

import java.sql.Connection;

import static org.jooq.SQLDialect.MARIADB;

public class MariaDbSink extends JooqSink {

    private String schemaName;

    public MariaDbSink(Connection dbConnection, String schema) {
        super(dbConnection, MARIADB, schema);
        this.schemaName = schema;
    }
}
