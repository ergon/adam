package ch.ergon.adam.integrationtest.postgresql;

import ch.ergon.adam.integrationtest.AssertAnyChangeStrategy;
import ch.ergon.adam.core.db.SchemaDiffExtractor;
import ch.ergon.adam.core.db.interfaces.SchemaSource;
import ch.ergon.adam.core.db.schema.Schema;
import ch.ergon.adam.core.db.schema.View;
import ch.ergon.adam.core.helper.FileHelper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static ch.ergon.adam.core.helper.CollectorsHelper.createSchemaItemNameArray;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

public abstract class AbstractSchemaCoverageTest extends AbstractPostgresqlTestBase {

    private static Path tempFolder;

    @BeforeAll
    public static void setupTempFolder() throws IOException {
        tempFolder = Files.createTempDirectory("ADAMTableTest");
        tempFolder.toFile().deleteOnExit();
    }

    @AfterAll
    public static void cleanupTempFolder() throws IOException {
        FileHelper.deleteFolderRecursively(tempFolder);
    }

    private static final String CREATE_ENUM_SQL =
        "CREATE TYPE test_enum AS ENUM ('val1')";

    private static final String CREATE_TABLE_SQL =
        "CREATE TABLE test_table (" +
            "id BIGSERIAL PRIMARY KEY, " +
            "col1 NUMERIC NOT NULL UNIQUE, " +
            "col2 NUMERIC(10,2) NULL DEFAULT 10, " +
            "col3 text NULL, " +
            "col4 VARCHAR(10) NOT NULL, " +
            "col5 test_enum NULL, " +
            "col6 text[] NULL, " +
            "col7 uuid NULL" +
            ")";

    private static final String CREATE_TABLE_WITH_FK_SQL =
        "CREATE TABLE test_fktable (" +
            "id BIGINT PRIMARY KEY REFERENCES test_table(id) " +
            ")";

    private static final String CREATE_VIEW =
        "CREATE VIEW test_view AS (" +
            "SELECT * FROM test_table " +
            ")";

    private static final String CREATE_SEQUENCE =
        "CREATE SEQUENCE test_sequence";

    private static final String CREATE_VIEW2 =
        "CREATE VIEW depending_view AS (" +
            "SELECT * FROM test_view " +
            ")";


    @Test
    public void testSchemaCoverage() throws Exception {
        getSourceDbConnection().createStatement().execute(CREATE_ENUM_SQL);
        getSourceDbConnection().createStatement().execute(CREATE_TABLE_SQL);
        getSourceDbConnection().createStatement().execute(CREATE_TABLE_WITH_FK_SQL);
        getSourceDbConnection().createStatement().execute(CREATE_VIEW);
        getSourceDbConnection().createStatement().execute(CREATE_VIEW2);
        getSourceDbConnection().createStatement().execute(CREATE_SEQUENCE);
        SchemaSource source = getSourceDbSource();
        Schema finalSchema = executeTransformation(source);

        assertSchemaEquals(source.getSchema(), finalSchema);
    }

    protected abstract Schema executeTransformation(SchemaSource source) throws Exception;

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
}
