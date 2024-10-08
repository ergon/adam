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
			Path generated = tempFolder.resolve("test").resolve("tables").resolve("TestTableTable.java");
			assertTrue(generated.toFile().exists(), "Metamodel for test table must be generated");
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
}
