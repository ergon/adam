package ch.ergon.adam.oracle;

import ch.ergon.adam.jooq.JooqSqlExecutor;

import java.sql.Connection;
import java.util.List;

public class OracleSqlExecutor extends JooqSqlExecutor {

    public OracleSqlExecutor(String url, String schema) {
        super(url, schema);
    }

    public OracleSqlExecutor(Connection dbConnection, String schema) {
        super(dbConnection, schema);
    }

    @Override
    public void executeScript(String script) {
        OracleScriptParser parser = new OracleScriptParser(script);
        List<OracleScriptParser.SqlStatement> statements = parser.parse();
        for (OracleScriptParser.SqlStatement statement : statements) {
            super.executeScript(statement.statement());
        }
    }
}
