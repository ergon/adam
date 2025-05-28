package ch.ergon.adam.integrationtest.mariadb;

import ch.ergon.adam.integrationtest.testcases.DefaultTest;
import org.junit.jupiter.api.Test;

public class MariaDbDefaultTest extends DefaultTest {
    public MariaDbDefaultTest() {
        super(new MariaDbTestDbUrlProvider());
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
