package ch.ergon.adam.integrationtest.sqlite;

import ch.ergon.adam.integrationtest.testcases.CastFieldTypeTest;

import java.io.IOException;

import static org.jooq.SQLDialect.SQLITE;

public class SqliteCastFieldTypeTests extends CastFieldTypeTest {
    public SqliteCastFieldTypeTests() throws IOException {
        super(new SqliteTestFileDbUrlProvider(), SQLITE);
    }
}
