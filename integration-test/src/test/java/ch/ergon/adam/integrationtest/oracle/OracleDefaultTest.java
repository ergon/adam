package ch.ergon.adam.integrationtest.oracle;

import ch.ergon.adam.integrationtest.testcases.DefaultTest;
import org.junit.jupiter.api.Test;

public class OracleDefaultTest extends DefaultTest {
    public OracleDefaultTest() {
        super(new OracleTestDbUrlProvider());
    }

    protected String getCreateTableIntDefaultSql() {
        return "create table \"test_table\" (" +
            "\"id\" number default 1 " +
            ")";
    }

    protected String getCreateTableStringDefaultSql() {
        return "create table \"test_table\" (" +
            "\"id\" VARCHAR(100) default 'defaultValue' " +
            ")";
    }

    protected String getCreateTableFunctionDefaultSql() {
        return "create table \"test_table\" (" +
            "\"id\" varchar(100) default SYS_GUID() " +
            ")";
    }

    @Test
    @Override
    public void testFunctionDefault() throws Exception {
        doTestDefault(getCreateTableFunctionDefaultSql(), "SYS_GUID()");
    }
}
