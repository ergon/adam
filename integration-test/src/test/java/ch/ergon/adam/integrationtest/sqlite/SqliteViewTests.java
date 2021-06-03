package ch.ergon.adam.integrationtest.sqlite;

import ch.ergon.adam.integrationtest.testcases.ViewTests;
import org.junit.jupiter.api.Disabled;

import java.io.IOException;

public class SqliteViewTests extends ViewTests {
    public SqliteViewTests() throws IOException {
        super(new SqliteTestFileDbUrlProvider());
    }

    @Override
    @Disabled
    public void testRecreateViewsAfterTableChange() throws Exception {
        //TODO: View dependency not supported
    }
}
