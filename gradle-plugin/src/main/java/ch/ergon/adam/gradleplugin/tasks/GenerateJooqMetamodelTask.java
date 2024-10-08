package ch.ergon.adam.gradleplugin.tasks;

import java.io.ByteArrayInputStream;
import java.nio.file.Path;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;
import org.jooq.codegen.GenerationTool;
import org.jooq.meta.jaxb.Configuration;
import org.jooq.meta.jaxb.Database;
import org.jooq.meta.jaxb.Generate;
import org.jooq.meta.jaxb.Generator;
import org.jooq.meta.jaxb.Property;
import org.jooq.meta.jaxb.Strategy;
import org.jooq.meta.jaxb.Target;

import ch.ergon.adam.gradleplugin.util.AdamDatabase;
import ch.ergon.adam.gradleplugin.util.TableSuffixGeneratorStrategy;

public class GenerateJooqMetamodelTask extends DefaultTask {

	@Input
	@Optional
	private String jooqConfig;

	// default ADAM?
	@Input
	@Optional
	private String schemaName;

	@Input
	private String packageName;

	@Input
	private Path outputPath;

	@Input
	private String ymlSource;

	@TaskAction
	void generateJooqMetamodel() throws Exception {
		Configuration configuration;
		if (jooqConfig != null) {
			configuration = GenerationTool.load(new ByteArrayInputStream(jooqConfig.getBytes("UTF-8")));
		} else {
			configuration = buildConfiguration();
		}
		GenerationTool.generate(configuration);
	}

	private Configuration buildConfiguration() {
		Configuration configuration;
		configuration = new Configuration();

		Generator generator = new Generator();
		Database database = new Database();
		database.setName(AdamDatabase.class.getName());
		database.getProperties().add(new Property().withKey(AdamDatabase.YML_SOURCE_PROPERTY).withValue(ymlSource));
		generator.setDatabase(database);

		Strategy strategy = new Strategy();
		strategy.setName(TableSuffixGeneratorStrategy.class.getName());
		generator.setStrategy(strategy);

		Generate generate = new Generate();
		generate.setGlobalObjectReferences(true);
		generate.setJavaTimeTypes(true);
		generator.setGenerate(generate);

		Target target = new Target();
		target.setPackageName(getPackageName());
		target.setDirectory(outputPath.toString());
		generator.setTarget(target);
		configuration.setGenerator(generator);
		return configuration;
	}

	public String getJooqConfig() {
		return jooqConfig;
	}

	public void setJooqConfig(String jooqConfig) {
		this.jooqConfig = jooqConfig;
	}

	public String getSchemaName() {
		return schemaName;
	}

	public void setSchemaName(String schemaName) {
		this.schemaName = schemaName;
	}

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public Path getOutputPath() {
		return outputPath;
	}

	public void setOutputPath(Path outputPath) {
		this.outputPath = outputPath;
	}

}
