package ch.ergon.adam.integrationtest.sqlite;

import ch.ergon.adam.integrationtest.testcases.DefaultTest;
import org.junit.jupiter.api.Disabled;

import java.io.IOException;

import static org.jooq.SQLDialect.SQLITE;

public class SqliteDefaultTest extends DefaultTest {
    public SqliteDefaultTest() throws IOException {
        super(new SqliteTestFileDbUrlProvider(), SQLITE);
    }

    @Disabled
    @Override
    public void testFunctionDefault() {
        //Functions as default not supported by sqlite
    }
}
