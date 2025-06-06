package ch.ergon.adam.gradleplugin.tasks;

import ch.ergon.adam.gradleplugin.util.AdamJooqMetamodelGenerator;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;

import java.nio.file.Path;

public class GenerateJooqMetamodelTask extends DefaultTask {

    @Input
    @Optional
    private String schemaName;

    @Input
    @Optional
    private String jooqConfig;

    @Input
    private String packageName;

    @Input
    private String outputPath;

    @Input
    private String source;

    @TaskAction
    public void generateJooqMetamodel() throws Exception {
        Path output = getProject().getLayout().getProjectDirectory().dir(outputPath).getAsFile().toPath();
        AdamJooqMetamodelGenerator generator = new AdamJooqMetamodelGenerator(schemaName, packageName, output, getSource(),
            jooqConfig);
        generator.run();
    }

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
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

    public String getOutputPath() {
        return outputPath;
    }

    public void setOutputPath(String outputPath) {
        this.outputPath = outputPath;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

}
