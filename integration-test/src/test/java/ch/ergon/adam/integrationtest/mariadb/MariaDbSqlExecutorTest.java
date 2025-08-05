package ch.ergon.adam.integrationtest.mariadb;

import ch.ergon.adam.integrationtest.testcases.SqlExecutorTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;

import java.sql.SQLException;

import static org.jooq.SQLDialect.MARIADB;

public class MariaDbSqlExecutorTest extends SqlExecutorTest {

    private static final MariaDbTestDbUrlProvider TEST_DB_URL_PROVIDER = new MariaDbTestDbUrlProvider();

    public MariaDbSqlExecutorTest() {
        super(TEST_DB_URL_PROVIDER, MARIADB);
    }

    @Override
    protected void verifyDroppedSchema() {
        Exception e = Assertions.assertThrows(SQLException.class, () -> getTargetDbConnection());
        Assertions.assertTrue(e.getMessage().contains("Unknown database 'test-target'"));
    }

    @AfterAll
    public static void restartContainer() {
        // MariaDB drops the whole database when dropping the schema, so we'll need to recreate the database
        // as test containers are shared between tests.
        TEST_DB_URL_PROVIDER.restartContainers();
    }
}
