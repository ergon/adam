package ch.ergon.adam.integrationtest.debug;

import ch.ergon.adam.core.db.interfaces.SchemaSource;
import ch.ergon.adam.core.db.schema.Schema;
import ch.ergon.adam.integrationtest.AbstractDbTestBase;
import ch.ergon.adam.yml.YmlFactory;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static ch.ergon.adam.core.Adam.DEFAULT_ADAM_PACKAGE;
import static ch.ergon.adam.core.Adam.DEFAULT_MAIN_RESOURCE_PATH;

public class TestDebugSchema extends AbstractDbTestBase {

    public TestDebugSchema() {
        super(new PostgreSqlForDebugTestDbUrlProvider());
    }

    @Test
    @Disabled
    public void debug() throws Exception {

        SchemaSource schemaSource = new YmlFactory().createSource("yml://../integration-test-db/" + DEFAULT_MAIN_RESOURCE_PATH + DEFAULT_ADAM_PACKAGE + "schema_debug");
        Schema sourceSchema = schemaSource.getSchema();
        this.migrateTargetWithSchema(sourceSchema);
    }
}
