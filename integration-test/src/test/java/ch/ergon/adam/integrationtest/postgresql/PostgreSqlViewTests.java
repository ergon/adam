package ch.ergon.adam.integrationtest.postgresql;

import ch.ergon.adam.integrationtest.testcases.ViewTests;

public class PostgreSqlViewTests extends ViewTests {
    public PostgreSqlViewTests() {
        super(new PostgreSqlTestDbUrlProvider());
    }
}
