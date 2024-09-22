package ch.ergon.adam.integrationtest.oracle;

import ch.ergon.adam.integrationtest.testcases.IndexTests;
import org.junit.jupiter.api.Disabled;

public class OracleIndexTests extends IndexTests {
    public OracleIndexTests() {
        super(new OracleTestDbUrlProvider());
    }

    protected String getCreateTableSql() {
        return "create table \"test_table\" (" +
            "\"id\" integer null unique, " +
            "\"col1\" varchar(100)" +
            ")";
    }

    @Override
    @Disabled
    public void testRecreatePartialIndexAfterTableChange() throws Exception {
        super.testRecreatePartialIndexAfterTableChange();
    }
}