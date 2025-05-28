package ch.ergon.adam.integrationtest.mariadb;

import ch.ergon.adam.integrationtest.testcases.DefaultTest;

import static org.jooq.SQLDialect.MARIADB;

public class MariaDbDefaultTest extends DefaultTest {
    public MariaDbDefaultTest() {
        super(new MariaDbTestDbUrlProvider(), MARIADB);
    }

    protected String getCreateTableStringDefaultSql() {
        return "create table test_table (" +
            "id VARCHAR(255) default 'defaultValue' " +
            ")";
    }
}
