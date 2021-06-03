package ch.ergon.adam.integrationtest.postgresql;

import ch.ergon.adam.integrationtest.testcases.AddFieldTests;

public class PostgreSqlAddFieldTests extends AddFieldTests {
    public PostgreSqlAddFieldTests() {
        super(new PostgreSqlTestDbUrlProvider());
    }
}
