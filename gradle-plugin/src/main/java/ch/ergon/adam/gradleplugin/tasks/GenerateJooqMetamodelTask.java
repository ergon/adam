package ch.ergon.adam.gradleplugin.tasks;

import java.nio.file.Path;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;

import ch.ergon.adam.gradleplugin.util.AdamJooqMetamodelGenerator;

public class GenerateJooqMetamodelTask extends DefaultTask {

	@Input
	@Optional
	private String jooqConfig;

	@Input
	private String packageName;

	@Input
	private Path outputPath;

	@Input
	private String source;

	@TaskAction
	public void generateJooqMetamodel() throws Exception {
		AdamJooqMetamodelGenerator generator = new AdamJooqMetamodelGenerator(packageName, outputPath, getSource(),
				jooqConfig);
		generator.run();
	}

	public String getJooqConfig() {
		return jooqConfig;
	}

	public void setJooqConfig(String jooqConfig) {
		this.jooqConfig = jooqConfig;
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

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

}
