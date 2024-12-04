package ch.ergon.adam.integrationtest.plugin;

import static ch.ergon.adam.core.Adam.DEFAULT_SCHEMA_PACKAGE;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import ch.ergon.adam.gradleplugin.util.AdamJooqMetamodelGenerator;

public class GenerateJooqMetamodelTest {

    private static Path tempFolder;

    @BeforeAll
    public static void setupTempFolder() throws IOException {
        tempFolder = Files.createTempDirectory("MetamodelTest");
        tempFolder.toFile().deleteOnExit();
    }

    @Test
    public void testMetamodelGeneration() {
        AdamJooqMetamodelGenerator generator = new AdamJooqMetamodelGenerator("test", tempFolder,
            "yml-classpath://" + DEFAULT_SCHEMA_PACKAGE, null);
        try {
            generator.run();
            Path tableFilePath = tempFolder.resolve("test").resolve("tables").resolve("TestTableTable.java");
            assertTrue(tableFilePath.toFile().exists(), "Metamodel for test table must be generated");

            Path pojoFilePath = tempFolder.resolve("test").resolve("tables").resolve("pojos").resolve("TestTable.java");
            assertTrue(pojoFilePath.toFile().exists(), "Metamodel for test table pojo must be generated");

            Path enumFilePath = tempFolder.resolve("test").resolve("enums").resolve("TestStatus.java");
            assertTrue(enumFilePath.toFile().exists(), "Metamodel for enums must be generated");

            Path sequencesFilePath = tempFolder.resolve("test").resolve("Sequences.java");
            assertTrue(sequencesFilePath.toFile().exists(), "Metamodel for sequences must be generated");
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }
}
