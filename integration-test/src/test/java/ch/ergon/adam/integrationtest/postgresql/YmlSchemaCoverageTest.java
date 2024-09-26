package ch.ergon.adam.integrationtest.postgresql;

import ch.ergon.adam.core.db.SchemaMigrator;
import ch.ergon.adam.core.db.interfaces.SchemaSource;
import ch.ergon.adam.core.db.schema.Schema;
import ch.ergon.adam.core.filetree.DirectoryTraverser;
import ch.ergon.adam.core.helper.FileHelper;
import ch.ergon.adam.integrationtest.EmptySource;
import ch.ergon.adam.yml.YmlSink;
import ch.ergon.adam.yml.YmlSource;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class YmlSchemaCoverageTest extends AbstractPostgreSQLSchemaCoverageTest {

    private static Path tempFolder;

    @BeforeAll
    public static void setupTempFolder() throws IOException {
        tempFolder = Files.createTempDirectory("YmlSchemaCoverageTest");
        tempFolder.toFile().deleteOnExit();
    }

    @AfterAll
    public static void cleanupTempFolder() throws IOException {
        FileHelper.deleteFolderRecursively(tempFolder);
    }

    @Override
    protected Schema executeTransformation(SchemaSource source) {
        File testTempPath = tempFolder.toFile();
        YmlSink sink = new YmlSink(testTempPath);
        new SchemaMigrator(new EmptySource(), source, sink).migrate();
        YmlSource ymlSource = new YmlSource(new DirectoryTraverser(testTempPath.toPath()));
        return ymlSource.getSchema();
    }
}
