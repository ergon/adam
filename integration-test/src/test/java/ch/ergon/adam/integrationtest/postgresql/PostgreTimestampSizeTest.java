package ch.ergon.adam.integrationtest.postgresql;

import ch.ergon.adam.core.db.SchemaDiffExtractor;
import ch.ergon.adam.core.db.interfaces.SchemaSource;
import ch.ergon.adam.core.db.schema.*;
import ch.ergon.adam.integrationtest.AbstractDbTestBase;
import ch.ergon.adam.integrationtest.AssertAnyChangeStrategy;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static ch.ergon.adam.core.db.schema.DataType.TIMESTAMPWITHTIMEZONE;
import static ch.ergon.adam.core.helper.CollectorsHelper.createSchemaItemNameArray;
import static com.google.common.collect.Lists.newArrayList;
import static org.jooq.SQLDialect.POSTGRES;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

public class PostgreTimestampSizeTest extends AbstractDbTestBase {

    public PostgreTimestampSizeTest() {
        super(new PostgreSqlTestDbUrlProvider(), POSTGRES);
    }

    @Test
    public void testCreateTable() {
        SchemaWithTimestampSource source = new SchemaWithTimestampSource();
        migrateTargetWithSchema(source.getSchema());
        Schema targetSchema = getTargetDbSource().getSchema();
        assertSchemaEquals(source.getSchema(), targetSchema);
    }

    private void assertSchemaEquals(Schema sourceSchema, Schema targetSchema) {
        SchemaDiffExtractor diffExtractor = new SchemaDiffExtractor(sourceSchema, targetSchema);
        diffExtractor.process(new AssertAnyChangeStrategy());

        sourceSchema.getViews().forEach(sourceView -> {
            View targetView = targetSchema.getView(sourceView.getName());
            String[] sourceDependencies = createSchemaItemNameArray(sourceView.getBaseRelations());
            String[] targetDependencies = createSchemaItemNameArray(targetView.getBaseRelations());
            assertArrayEquals(targetDependencies, sourceDependencies);
        });
    }

    private class SchemaWithTimestampSource implements SchemaSource {

        @Override
        public Schema getSchema() {
            Schema schema = new Schema();
            Table table = new Table("test_table");
            Field executionStartedAt = createField("execution_started_at", TIMESTAMPWITHTIMEZONE);
            table.setFields(newArrayList(executionStartedAt));
            schema.setTables(newArrayList(table));
            return schema;
        }

        private Field createField(String name, DataType type) {
            Field field = new Field(name);
            field.setDataType(type);
            return field;

        }

        @Override
        public void close() throws IOException {

        }
    }
}
