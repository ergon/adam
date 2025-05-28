package ch.ergon.adam.integrationtest.sqlite;

import ch.ergon.adam.integrationtest.testcases.IndexTests;
import org.junit.jupiter.api.Disabled;

import java.io.IOException;

import static org.jooq.SQLDialect.SQLITE;

public class SqliteIndexTests extends IndexTests {
    public SqliteIndexTests() throws IOException {
        super(new SqliteTestFileDbUrlProvider(), SQLITE);
    }

    @Override
    @Disabled
    public void testRecreatePartialIndexAfterTableChange() throws Exception {
        // https://github.com/jOOQ/jOOQ/issues/16683
    }
}
