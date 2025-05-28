package ch.ergon.adam.integrationtest.mariadb;

import ch.ergon.adam.integrationtest.testcases.AddSqlForNewTest;

import static org.jooq.SQLDialect.MARIADB;

public class MariaDbAddSqlForNewTest extends AddSqlForNewTest {
    public MariaDbAddSqlForNewTest() {
        super(new MariaDbTestDbUrlProvider(), MARIADB);
    }

    @Override
    protected String getCreateEnumSql() {
        // MariaDB does not support enum type
        return "SELECT true FROM DUAL";
    }
}
