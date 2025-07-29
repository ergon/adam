package ch.ergon.adam.integrationtest.oracle;

import ch.ergon.adam.integrationtest.testcases.SqlExecutorTest;

import static org.jooq.SQLDialect.ORACLE;

public class OracleSqlExecutorTest extends SqlExecutorTest {
    public OracleSqlExecutorTest() {
        super(new OracleTestDbUrlProvider(), ORACLE);
    }
}
