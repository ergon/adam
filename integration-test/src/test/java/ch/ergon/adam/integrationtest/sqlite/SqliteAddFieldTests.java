package ch.ergon.adam.integrationtest.sqlite;

import ch.ergon.adam.integrationtest.testcases.AddFieldTests;

import java.io.IOException;

public class SqliteAddFieldTests extends AddFieldTests {
    public SqliteAddFieldTests() throws IOException {
        super(new SqliteTestFileDbUrlProvider());
    }
}
