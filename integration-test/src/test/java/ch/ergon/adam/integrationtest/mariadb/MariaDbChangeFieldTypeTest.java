package ch.ergon.adam.integrationtest.mariadb;

import ch.ergon.adam.integrationtest.testcases.ChangeFieldTypeTest;
import org.junit.jupiter.api.Disabled;

public class MariaDbChangeFieldTypeTest extends ChangeFieldTypeTest {
    public MariaDbChangeFieldTypeTest() {
        super(new MariaDbTestDbUrlProvider());
    }

    protected String getCreateTableNotNullSql() {
        return "create table \"test_table\" (" +
            "\"test_field\" varchar(10) not null " +
            ")";
    }

    protected String getCreateTableNullSql() {
        return "create table \"test_table\" (" +
            "\"test_field\" numeric(10,2) null " +
            ")";
    }

    protected String getCreateTableTwoFieldsSql() {
        return "create table \"test_table\" (" +
            "\"col1\" clob null, " +
            "\"col2\" clob null " +
            ")";
    }

    @Override
    @Disabled
    public void changeFromSerialToClob() {
    }
}
