package ch.ergon.adam.jooq;

import ch.ergon.adam.core.db.interfaces.SqlExecutor;
import org.jooq.CloseableDSLContext;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.impl.DSL;

import java.sql.Connection;
import java.sql.SQLException;

public class JooqSqlExecutor implements SqlExecutor {

    protected final DSLContext context;
    private final String schema;
    private final Connection connection;

    public JooqSqlExecutor(String url, String schema) {
        context = DSL.using(url);
        this.schema = schema;
        this.connection = null;
    }

    public JooqSqlExecutor(Connection dbConnection, String schema) {
        context = DSL.using(dbConnection);
        this.schema = schema;
        this.connection = dbConnection;
    }

    @Override
    public void close() {
        if (context instanceof CloseableDSLContext) {
            ((CloseableDSLContext) context).close();
        }
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void executeScript(String script) {
        context.execute(script);
    }

    @Override
    public Object queryResult(String query, Object... params) {
        Result<Record> result = context.resultQuery(query, params).fetch();
        if (result.isNotEmpty()) {
            return result.getValue(0, 0);
        }
        return null;
    }

    @Override
    public void rollback() {

    }

    @Override
    public void dropSchema() {
        context.dropSchema(schema).cascade().execute();
    }
}
