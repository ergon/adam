package ch.ergon.adam.integrationtest.oracle;

import ch.ergon.adam.integrationtest.testcases.RemoveFieldTests;

import static org.jooq.SQLDialect.ORACLE;

public class OracleRemoveFieldTests extends RemoveFieldTests {
    public OracleRemoveFieldTests() {
        super(new OracleTestDbUrlProvider(), ORACLE);
    }
}
