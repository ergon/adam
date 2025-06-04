package ch.ergon.adam.integrationtest.sqlite;

import java.io.IOException;

public class SqliteFileCreateTableTest extends SqliteCreateTableTest {
    public SqliteFileCreateTableTest() throws IOException {
        super(new SqliteTestFileDbUrlProvider());
    }
}
