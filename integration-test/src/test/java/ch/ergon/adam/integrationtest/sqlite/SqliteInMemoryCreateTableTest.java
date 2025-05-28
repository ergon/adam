package ch.ergon.adam.integrationtest.sqlite;

import static org.jooq.SQLDialect.SQLITE;

public class SqliteInMemoryCreateTableTest extends SqliteCreateTableTest {
    public SqliteInMemoryCreateTableTest() {
        super(new SqliteTestInMemoryDbUrlProvider());
    }
}
