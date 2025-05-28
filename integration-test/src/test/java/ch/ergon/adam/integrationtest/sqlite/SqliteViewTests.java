package ch.ergon.adam.integrationtest.sqlite;

import ch.ergon.adam.integrationtest.testcases.ViewTests;
import org.junit.jupiter.api.Disabled;

import java.io.IOException;

import static org.jooq.SQLDialect.SQLITE;

public class SqliteViewTests extends ViewTests {
    public SqliteViewTests() throws IOException {
        super(new SqliteTestFileDbUrlProvider(), SQLITE);
    }

    @Override
    @Disabled
    public void testRecreateViewsAfterTableChange() throws Exception {
        //TODO: View dependency not supported
    }
}
