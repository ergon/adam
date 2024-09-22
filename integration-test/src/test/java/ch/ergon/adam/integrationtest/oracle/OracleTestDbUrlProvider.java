package ch.ergon.adam.integrationtest.oracle;

import ch.ergon.adam.integrationtest.TestDbUrlProvider;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.testcontainers.oracle.OracleContainer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.Duration;

public class OracleTestDbUrlProvider extends TestDbUrlProvider {

    private static final OracleContainer sourceContainer = new OracleContainer("gvenzl/oracle-free:23.4-slim-faststart");
    private static final OracleContainer targetContainer = new OracleContainer("gvenzl/oracle-free:23.4-slim-faststart");

    @Override
    protected void initDbForTest() throws SQLException {
        try (Connection conn = DriverManager.getConnection(getDbUrl(sourceContainer))) {
            cleanSchema(conn);
        }
        try (Connection conn = DriverManager.getConnection(getDbUrl(targetContainer))) {
            cleanSchema(conn);
        }
    }

    private void cleanSchema(Connection conn) throws SQLException {
        DSLContext dslContext = DSL.using(conn);
        dslContext.execute("""
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
                                              'MATERIALIZED VIEW'
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

    @Override
    protected String getSourceDbUrl() {
        return getDbUrl(sourceContainer);
    }

    @Override
    protected String getTargetDbUrl() {
        return getDbUrl(targetContainer);
    }

    protected String getDbUrl(OracleContainer container) {
        if (!container.isRunning()) {
            container.withStartupTimeout(Duration.ofMinutes(5));
            container.start();
        }
        return container.getJdbcUrl().replace("@", container.getUsername() +"/" + container.getPassword() + "@") + "?currentSchema=" + container.getUsername().toUpperCase();
    }
}
