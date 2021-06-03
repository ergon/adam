package ch.ergon.adam.integrationtest.sqlite;

import ch.ergon.adam.integrationtest.testcases.RemoveFieldTests;

import java.io.IOException;

public class SqliteRemoveFieldTests extends RemoveFieldTests {
    public SqliteRemoveFieldTests() throws IOException {
        super(new SqliteTestFileDbUrlProvider());
    }
}
