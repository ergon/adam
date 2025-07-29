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

    @Override
    public void dropSchema() {
        context.execute("""
            BEGIN
               FOR cur_rec IN (SELECT object_name, object_type
                                 FROM user_objects
                                WHERE object_type IN
                                         ('TABLE',
                                          'VIEW',
                                          'PACKAGE',
                                          'PROCEDURE',
                                          'FUNCTION',
                                          'SEQUENCE',
                                          'TYPE',
                                          'SYNONYM',
                                          'MATERIALIZED VIEW',
                                          'SCHEMA'
                                         ))
               LOOP
                  BEGIN
                     IF cur_rec.object_type = 'TABLE'
                     THEN
                        EXECUTE IMMEDIATE    'DROP '
                                          || cur_rec.object_type
                                          || ' "'
                                          || cur_rec.object_name
                                          || '" CASCADE CONSTRAINTS';
                     ELSE
                        EXECUTE IMMEDIATE    'DROP '
                                          || cur_rec.object_type
                                          || ' "'
                                          || cur_rec.object_name
                                          || '"';
                     END IF;
                  EXCEPTION
                     WHEN OTHERS
                     THEN
                        DBMS_OUTPUT.put_line (   'FAILED: DROP '
                                              || cur_rec.object_type
                                              || ' "'
                                              || cur_rec.object_name
                                              || '"'
                                             );
                  END;
               END LOOP;
            END;
            """);
    }
}
