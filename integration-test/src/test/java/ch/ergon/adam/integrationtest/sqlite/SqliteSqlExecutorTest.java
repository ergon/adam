package ch.ergon.adam.integrationtest.sqlite;

import ch.ergon.adam.core.db.SourceAndSinkFactory;
import ch.ergon.adam.core.db.interfaces.SqlExecutor;
import ch.ergon.adam.integrationtest.testcases.SqlExecutorTest;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.jooq.SQLDialect.SQLITE;

public class SqliteSqlExecutorTest extends SqlExecutorTest {

    public SqliteSqlExecutorTest() throws IOException {
        super(new SqliteTestFileDbUrlProvider(), SQLITE);
    }

    @Test
    public void testDropSchema() throws Exception {
        executeOnTargetDb(CREATE_TABLE_SQL);
        executeOnTargetDb(INSERT_DATA_SQL);

        String targetUrl = getTargetDbUrl();
        try (
            SqlExecutor sqlExecutor = SourceAndSinkFactory.getInstance().getSqlExecutor(targetUrl)
        ) {
            Assert.assertThrows(UnsupportedOperationException.class, () -> sqlExecutor.dropSchema());
        }
    }
}
