package ch.ergon.adam.integrationtest.sqlite;

import ch.ergon.adam.integrationtest.testcases.RenameTableTests;

import java.io.IOException;

public class SqliteRenameTableTests extends RenameTableTests {
    public SqliteRenameTableTests() throws IOException {
        super(new SqliteTestFileDbUrlProvider());
    }
}
