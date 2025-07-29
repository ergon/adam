package ch.ergon.adam.integrationtest.testcases;

import ch.ergon.adam.core.db.SourceAndSinkFactory;
import ch.ergon.adam.core.db.interfaces.SqlExecutor;
import ch.ergon.adam.integrationtest.AbstractDbTestBase;
import ch.ergon.adam.integrationtest.TestDbUrlProvider;
import org.jooq.SQLDialect;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class SqlExecutorTest extends AbstractDbTestBase {

    protected static final String CREATE_TABLE_SQL =
        "create table \"test_table\" (" +
            "col1 int" +
            ")";

    protected static final String INSERT_DATA_SQL =
        "insert into \"test_table\" values (1)";

    public SqlExecutorTest(TestDbUrlProvider testDbUrlProvider, SQLDialect dialect) {
        super(testDbUrlProvider, dialect);
    }

    @Test
    public void testDropSchema() throws Exception {
        executeOnTargetDb(CREATE_TABLE_SQL);
        executeOnTargetDb(INSERT_DATA_SQL);
        TestDbUrlProvider.close();

        String targetUrl = getTargetDbUrl();
        try (
            SqlExecutor sqlExecutor = SourceAndSinkFactory.getInstance().getSqlExecutor(targetUrl)
        ) {
            sqlExecutor.dropSchema();
        }
        verifyDroppedSchema();
    }

    protected void verifyDroppedSchema() throws SQLException {
        ResultSet tempTable = getTargetDbConnection()
            .getMetaData()
            .getTables(null, getTargetDbConnection().getSchema(), "test_table", null);
        Assertions.assertFalse(tempTable.next());
    }
}
