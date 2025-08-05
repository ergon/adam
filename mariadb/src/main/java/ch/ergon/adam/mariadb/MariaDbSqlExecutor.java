package ch.ergon.adam.mariadb;

import ch.ergon.adam.jooq.JooqSqlExecutor;

import java.sql.Connection;

public class MariaDbSqlExecutor extends JooqSqlExecutor {

    private final String schema;

    public MariaDbSqlExecutor(String url, String schema) {
        super(url, schema);
        this.schema = schema;
    }

    public MariaDbSqlExecutor(Connection dbConnection, String schema) {
        super(dbConnection, schema);
        this.schema = schema;
    }

    @Override
    public void dropSchema() {
        context.dropSchema(schema).execute();
    }
}
