package ch.ergon.adam.integrationtest.mariadb;

import ch.ergon.adam.integrationtest.testcases.CastFieldTypeTest;

import static org.jooq.SQLDialect.MARIADB;

public class MariaDbCastFieldTypeTests extends CastFieldTypeTest {
    public MariaDbCastFieldTypeTests() {
        super(new MariaDbTestDbUrlProvider(), MARIADB);
    }

    @Override
    protected String getInsertDataStatement() {
        return "INSERT INTO test_table VALUES ('2', 2)";
    }

    @Override
    protected String getCreateTableStatement() {
        return
            "CREATE TABLE test_table (" +
                "col1 VARCHAR(255), " +
                "col2 INT " +
                ")";
    }
}
