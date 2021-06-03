package ch.ergon.adam.integrationtest.postgresql;

import ch.ergon.adam.integrationtest.testcases.RenameTableTests;

public class PostgreSqlRenameTableTests extends RenameTableTests {
    public PostgreSqlRenameTableTests() {
        super(new PostgreSqlTestDbUrlProvider());
    }
}
