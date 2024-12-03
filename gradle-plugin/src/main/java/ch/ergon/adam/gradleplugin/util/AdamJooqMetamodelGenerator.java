package ch.ergon.adam.gradleplugin.util;

import java.io.ByteArrayInputStream;
import java.nio.file.Path;

import org.jooq.codegen.GenerationTool;
import org.jooq.meta.jaxb.*;

public class AdamJooqMetamodelGenerator {

    private final String packageName;
    private final Path outputPath;
    private final String source;
    private final String jooqConfig;

    public AdamJooqMetamodelGenerator(String packageName, Path outputPath, String source, String jooqConfig) {
        this.packageName = packageName;
        this.outputPath = outputPath;
        this.source = source;
        this.jooqConfig = jooqConfig;
    }

    public void run() throws Exception {
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
        database.getProperties().add(new Property().withKey(AdamDatabase.SOURCE_PROPERTY).withValue(source));
        generator.setDatabase(database);

        Strategy strategy = new Strategy();
        strategy.setName(TableSuffixGeneratorStrategy.class.getName());
        generator.setStrategy(strategy);

        Generate generate = new Generate();
        generate.setGlobalObjectReferences(true);
        generate.setJavaTimeTypes(true);
        generate.setPojos(true);
        generate.setFluentSetters(true);
        generator.setGenerate(generate);

        Target target = new Target();
        target.setPackageName(packageName);
        target.setDirectory(outputPath.toString());
        generator.setTarget(target);
        configuration.setGenerator(generator);
        return configuration;
    }

}
