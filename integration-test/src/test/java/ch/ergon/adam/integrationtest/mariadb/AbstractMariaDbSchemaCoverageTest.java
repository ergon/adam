package ch.ergon.adam.integrationtest.mariadb;

import ch.ergon.adam.core.db.SchemaDiffExtractor;
import ch.ergon.adam.core.db.interfaces.SchemaSource;
import ch.ergon.adam.core.db.schema.Schema;
import ch.ergon.adam.core.db.schema.View;
import ch.ergon.adam.core.helper.FileHelper;
import ch.ergon.adam.integrationtest.AssertAnyChangeStrategy;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static ch.ergon.adam.core.helper.CollectorsHelper.createSchemaItemNameArray;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

public abstract class AbstractMariaDbSchemaCoverageTest extends AbstractMariaDbTestBase {

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

    private static final String CREATE_TABLE_SQL =
        "CREATE TABLE test_table (" +
            "id INTEGER AUTO_INCREMENT PRIMARY KEY, " +
            "col1 INTEGER NOT NULL, " +
            "col2 DECIMAL(10,2) DEFAULT 10 NULL, " +
            "col3 LONGTEXT NULL, " +
            "col4 VARCHAR(10) NOT NULL, " +
            "CONSTRAINT test_table_col1_key UNIQUE (col1)" +
            ")";


    private static final String CREATE_TABLE_WITH_FK_SQL =
        "CREATE TABLE test_fktable (" +
            "id INTEGER, " +
            "FOREIGN KEY (id) REFERENCES test_table(id)" +
            ")";

    private static final String CREATE_VIEW =
        "CREATE VIEW test_view AS " +
            "SELECT * FROM test_table";

    private static final String CREATE_SEQUENCE =
        "CREATE SEQUENCE test_sequence";

    private static final String CREATE_VIEW2 =
        "CREATE VIEW depending_view AS " +
            "SELECT * FROM test_view";


    @Test
    public void testSchemaCoverage() throws Exception {
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
