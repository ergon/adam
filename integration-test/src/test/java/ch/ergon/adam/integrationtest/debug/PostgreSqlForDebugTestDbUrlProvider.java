package ch.ergon.adam.integrationtest.debug;

import ch.ergon.adam.integrationtest.postgresql.PostgreSqlTestDbUrlProvider;

import java.sql.SQLException;

public class PostgreSqlForDebugTestDbUrlProvider extends PostgreSqlTestDbUrlProvider {

    @Override
    protected void initDbForTest() throws SQLException {
        // Do not recreate schema
    }
}
