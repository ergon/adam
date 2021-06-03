package ch.ergon.adam.integrationtest.sqlite;

import ch.ergon.adam.integrationtest.testcases.IndexTests;

import java.io.IOException;

public class SqliteIndexTests extends IndexTests {
    public SqliteIndexTests() throws IOException {
        super(new SqliteTestFileDbUrlProvider());
    }
}
