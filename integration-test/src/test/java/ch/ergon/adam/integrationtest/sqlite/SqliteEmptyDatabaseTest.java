package ch.ergon.adam.integrationtest.sqlite;

import ch.ergon.adam.integrationtest.testcases.EmptyDatabaseTest;

import java.io.IOException;

import static org.jooq.SQLDialect.SQLITE;

public class SqliteEmptyDatabaseTest extends EmptyDatabaseTest {
    public SqliteEmptyDatabaseTest() throws IOException {
        super(new SqliteTestFileDbUrlProvider(), SQLITE);
    }
}
