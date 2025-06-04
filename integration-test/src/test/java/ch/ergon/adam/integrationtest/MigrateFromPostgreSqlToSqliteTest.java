package ch.ergon.adam.integrationtest;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.jooq.SQLDialect.POSTGRES;

public class MigrateFromPostgreSqlToSqliteTest extends AbstractDbTestBase {

    private static final String CREATE_TABLE_SQL =
        "create table test_table (" +
            "id int " +
            ")";

    private static final String INSERT_DATA_SQL =
        "insert into test_table values (1)";

    public MigrateFromPostgreSqlToSqliteTest() throws IOException {
        super(new PostgreSqlToSqliteTestDbUrlProvider(), POSTGRES);
    }

    @Test
    public void testAddFieldAtEndOfTable() throws Exception {

        // Setup db
        getSourceDbConnection().createStatement().execute(CREATE_TABLE_SQL);
        getSourceDbConnection().createStatement().execute(INSERT_DATA_SQL);
        sourceToTarget();
    }
}
