package ch.ergon.adam.sqlite;

import ch.ergon.adam.jooq.JooqSqlExecutor;

import java.sql.Connection;

public class SqliteSqlExecutor extends JooqSqlExecutor {

    public SqliteSqlExecutor(String url, String schema) {
        super(url, schema);
    }

    public SqliteSqlExecutor(Connection dbConnection, String schema) {
        super(dbConnection, schema);
    }

    @Override
    public void dropSchema() {
        throw new UnsupportedOperationException("Drop schema not supported in Sqlite");
    }
}
