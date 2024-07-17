package ch.ergon.adam.integrationtest.postgresql;

import ch.ergon.adam.integrationtest.testcases.AddSqlForNewTest;

public class PostgreSqlAddSqlForNewTest extends AddSqlForNewTest {
    public PostgreSqlAddSqlForNewTest() {
        super(new PostgreSqlTestDbUrlProvider());
    }
}
