package ch.ergon.adam.integrationtest.postgresql;

import ch.ergon.adam.integrationtest.testcases.SqlExecutorTest;

import static org.jooq.SQLDialect.POSTGRES;

public class PostgreSqlExecutorTest extends SqlExecutorTest {
    public PostgreSqlExecutorTest() {
        super(new PostgreSqlTestDbUrlProvider(), POSTGRES);
    }
}
