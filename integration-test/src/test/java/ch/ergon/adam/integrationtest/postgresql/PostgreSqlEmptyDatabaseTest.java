package ch.ergon.adam.integrationtest.postgresql;

import ch.ergon.adam.integrationtest.testcases.EmptyDatabaseTest;

import static org.jooq.SQLDialect.POSTGRES;

public class PostgreSqlEmptyDatabaseTest extends EmptyDatabaseTest {
    public PostgreSqlEmptyDatabaseTest() {
        super(new PostgreSqlTestDbUrlProvider(), POSTGRES);
    }
}
