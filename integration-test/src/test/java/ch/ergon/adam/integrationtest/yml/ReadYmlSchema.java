package ch.ergon.adam.integrationtest.yml;

import ch.ergon.adam.core.db.interfaces.SchemaSource;
import ch.ergon.adam.core.db.schema.Schema;
import ch.ergon.adam.yml.YmlFactory;
import org.junit.jupiter.api.Test;

import static ch.ergon.adam.core.Adam.DEFAULT_MAIN_RESOURCE_PATH;
import static ch.ergon.adam.core.Adam.DEFAULT_SCHEMA_PACKAGE;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class ReadYmlSchema {

    @Test
    public void readFromClassPath() {
        SchemaSource schemaSource = new YmlFactory().createSource("yml-classpath://" + DEFAULT_SCHEMA_PACKAGE);
        Schema schema = schemaSource.getSchema();
        assertThat(schema.getTables().size(), is(1));
    }

    @Test
    public void readFromPath() {
        SchemaSource schemaSource = new YmlFactory().createSource("yml://../integration-test-db/" + DEFAULT_MAIN_RESOURCE_PATH + DEFAULT_SCHEMA_PACKAGE);
        Schema schema = schemaSource.getSchema();
        assertThat(schema.getTables().size(), is(1));
    }

}
