package ch.ergon.adam.integrationtest.sqlite;

import ch.ergon.adam.integrationtest.TestDbUrlProvider;
import ch.ergon.adam.sqlite.SqliteInMemoryFactory;
import ch.ergon.adam.core.helper.Pair;

import java.io.IOException;
import java.sql.Connection;

import static ch.ergon.adam.sqlite.SqliteInMemoryFactory.SQLITE_IN_MEMORY_URL;

public class SqliteTestInMemoryDbUrlProvider extends TestDbUrlProvider {

    @Override
    protected void initDbForTest() throws IOException {

    }

    @Override
    protected String getSourceDbUrl() {
        return getDbUrl("source");
    }

    @Override
    protected String getTargetDbUrl() {
        return getDbUrl("target");
    }

    @Override
    protected Pair<Connection, Runnable> createDbConnection(String url) {
        Connection connection = SqliteInMemoryFactory.getOrCreateConnection(url);
        Runnable closeHandler = () -> closeDbConnection(url);
        return new Pair(connection, closeHandler);
    }

    private void closeDbConnection(String url) {
        SqliteInMemoryFactory.closeConnection(url);
    }


    protected String getDbUrl(String name) {
        return SQLITE_IN_MEMORY_URL + "/" + name;
    }
}
