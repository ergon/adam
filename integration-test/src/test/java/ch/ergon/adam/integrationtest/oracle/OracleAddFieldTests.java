package ch.ergon.adam.integrationtest.oracle;

import ch.ergon.adam.integrationtest.testcases.AddFieldTests;

import static org.jooq.SQLDialect.ORACLE;

public class OracleAddFieldTests extends AddFieldTests {
    public OracleAddFieldTests() {
        super(new OracleTestDbUrlProvider(), ORACLE);
    }
}
