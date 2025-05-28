package ch.ergon.adam.integrationtest.postgresql;

import ch.ergon.adam.integrationtest.testcases.ChangeFieldTypeTest;

import static org.jooq.SQLDialect.POSTGRES;

public class PostgreSqlChangeFieldTypeTest extends ChangeFieldTypeTest {
    public PostgreSqlChangeFieldTypeTest() {
        super(new PostgreSqlTestDbUrlProvider(), POSTGRES);
    }
}
