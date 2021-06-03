package ch.ergon.adam.integrationtest.postgresql;

import ch.ergon.adam.integrationtest.testcases.RemoveFieldTests;

public class PostgreSqlRemoveFieldTests extends RemoveFieldTests {
    public PostgreSqlRemoveFieldTests() {
        super(new PostgreSqlTestDbUrlProvider());
    }
}
