package ch.ergon.adam.integrationtest.postgresql;

import ch.ergon.adam.integrationtest.testcases.ChangeArrayFieldTypeTest;

import static org.jooq.SQLDialect.POSTGRES;

public class PostgreSqlChangeArrayFieldTypeTest extends ChangeArrayFieldTypeTest {
    public PostgreSqlChangeArrayFieldTypeTest() {
        super(new PostgreSqlTestDbUrlProvider(), POSTGRES);
    }
}
