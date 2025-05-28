package ch.ergon.adam.integrationtest.oracle;

import ch.ergon.adam.integrationtest.testcases.EmptyDatabaseTest;

import static org.jooq.SQLDialect.ORACLE;

public class OracleEmptyDatabaseTest extends EmptyDatabaseTest {

    public OracleEmptyDatabaseTest() {
        super(new OracleTestDbUrlProvider(), ORACLE);
    }
}
