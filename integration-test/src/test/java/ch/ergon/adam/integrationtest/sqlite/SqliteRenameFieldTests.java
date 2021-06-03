package ch.ergon.adam.integrationtest.sqlite;

import ch.ergon.adam.integrationtest.testcases.RenameFieldTests;

import java.io.IOException;

public class SqliteRenameFieldTests extends RenameFieldTests {
    public SqliteRenameFieldTests() throws IOException {
        super(new SqliteTestFileDbUrlProvider());
    }
}
