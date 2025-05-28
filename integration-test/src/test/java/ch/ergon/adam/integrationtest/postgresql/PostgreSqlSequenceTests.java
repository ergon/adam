package ch.ergon.adam.integrationtest.postgresql;

import ch.ergon.adam.integrationtest.testcases.SequenceTests;

import static org.jooq.SQLDialect.POSTGRES;

public class PostgreSqlSequenceTests extends SequenceTests {
    public PostgreSqlSequenceTests() {
        super(new PostgreSqlTestDbUrlProvider(), POSTGRES);
    }
}
