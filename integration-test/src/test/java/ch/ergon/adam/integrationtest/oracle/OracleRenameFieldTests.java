package ch.ergon.adam.integrationtest.oracle;

import ch.ergon.adam.integrationtest.testcases.RenameFieldTests;

import static org.jooq.SQLDialect.ORACLE;

public class OracleRenameFieldTests extends RenameFieldTests {
    public OracleRenameFieldTests() {
        super(new OracleTestDbUrlProvider(), ORACLE);
    }
}
