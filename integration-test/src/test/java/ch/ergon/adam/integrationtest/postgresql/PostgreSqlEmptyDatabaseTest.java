package ch.ergon.adam.integrationtest.postgresql;

import ch.ergon.adam.integrationtest.testcases.EmptyDatabaseTest;

public class PostgreSqlEmptyDatabaseTest extends EmptyDatabaseTest {

    public PostgreSqlEmptyDatabaseTest() {
        super(new PostgreSqlTestDbUrlProvider());
    }
}
