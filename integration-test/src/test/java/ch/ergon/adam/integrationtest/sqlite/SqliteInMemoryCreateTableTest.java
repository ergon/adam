package ch.ergon.adam.integrationtest.sqlite;

public class SqliteInMemoryCreateTableTest extends SqliteCreateTableTest {

    public SqliteInMemoryCreateTableTest() {
        super(new SqliteTestInMemoryDbUrlProvider());
    }
}
