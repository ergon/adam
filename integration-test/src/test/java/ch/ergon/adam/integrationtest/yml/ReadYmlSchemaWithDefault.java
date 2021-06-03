package ch.ergon.adam.integrationtest.yml;

import ch.ergon.adam.core.db.interfaces.SchemaSource;
import ch.ergon.adam.core.db.schema.ForeignKey;
import ch.ergon.adam.core.db.schema.Schema;
import ch.ergon.adam.core.db.schema.Table;
import ch.ergon.adam.yml.YmlFactory;
import org.junit.jupiter.api.Test;

import static ch.ergon.adam.core.Adam.DEFAULT_ADAM_PACKAGE;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class ReadYmlSchemaWithDefault {

    @Test
    public void readFromClassPath() {
        SchemaSource schemaSource = new YmlFactory().createSource("yml-classpath://" + DEFAULT_ADAM_PACKAGE + "schema_yml_default_values");
        Schema schema = schemaSource.getSchema();
        assertThat(schema.getTables().size(), is(2));
        Table tableA = schema.getTable("table_a");
        Table tableB = schema.getTable("table_b");
        ForeignKey fk = tableB.getForeignKeys().stream().findAny().get();
        assertThat(fk.getName(), is("fk_table_b_0"));
        assertThat(fk.getTargetIndex(), is(tableA.getIndex("idx_table_a_0")));
    }
}
