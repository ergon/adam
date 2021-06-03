package ch.ergon.adam.integrationtest.postgresql;

import ch.ergon.adam.integrationtest.testcases.RenameFieldTests;

public class PostgreSqlRenameFieldTests extends RenameFieldTests {
    public PostgreSqlRenameFieldTests() {
        super(new PostgreSqlTestDbUrlProvider());
    }
}
