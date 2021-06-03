package ch.ergon.adam.integrationtest.postgresql;

import ch.ergon.adam.integrationtest.testcases.SequenceTests;

public class PostgreSqlSequenceTests extends SequenceTests {
    public PostgreSqlSequenceTests() {
        super(new PostgreSqlTestDbUrlProvider());
    }
}
