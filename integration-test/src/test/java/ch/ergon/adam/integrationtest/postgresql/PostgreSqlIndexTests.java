package ch.ergon.adam.integrationtest.postgresql;

import ch.ergon.adam.integrationtest.testcases.IndexTests;

public class PostgreSqlIndexTests extends IndexTests {
    public PostgreSqlIndexTests() {
        super(new PostgreSqlTestDbUrlProvider());
    }
}
