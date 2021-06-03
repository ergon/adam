package ch.ergon.adam.core.db;

import ch.ergon.adam.core.MigrationConfiguration;
import ch.ergon.adam.core.db.interfaces.MigrationStrategy;
import ch.ergon.adam.core.db.interfaces.SchemaSink;
import ch.ergon.adam.core.db.interfaces.SchemaSource;
import ch.ergon.adam.core.db.schema.Schema;
import ch.ergon.adam.core.db.schema.SchemaItem;

import static java.util.stream.Collectors.toList;

public class SchemaMigrator {

    private SchemaSource source;
    private SchemaSource reference;
    private SchemaSink sink;
    private MigrationConfiguration configuration = new MigrationConfiguration();

    public static void migrate(String sourceUrl, String targetUrl) {
        migrate(sourceUrl, targetUrl, new MigrationConfiguration());
    }

    public static void migrate(String sourceUrl, String targetUrl, MigrationConfiguration configuration) {
        SourceAndSinkFactory factory = SourceAndSinkFactory.getInstance();
        try (SchemaSource reference = factory.getSource(sourceUrl);
             SchemaSink sink = factory.getSink(targetUrl);
             SchemaSource source = factory.getSource(targetUrl)) {
            SchemaMigrator schemaMigrator = new SchemaMigrator(source, reference, sink);
            schemaMigrator.setConfiguration(configuration);
            try {
                schemaMigrator.migrate();
            } catch (Exception e) {
                sink.rollback();
                throw e;
            }
        } catch (Exception e) {
            throw new RuntimeException("Migration failed.", e);
        }
    }

    public SchemaMigrator(SchemaSource source, SchemaSource reference, SchemaSink sink) {
        this.source = source;
        this.reference = reference;
        this.sink = new LoggingSinkWrapper(sink);
    }

    public void migrate() {
        Schema sourceSchema = source.getSchema();
        Schema targetSchema = reference.getSchema();

        sourceSchema = filter(sourceSchema);
        targetSchema = filter(targetSchema);
        sink.setTargetSchema(targetSchema);
        SchemaDiffExtractor diffExtractor = new SchemaDiffExtractor(sourceSchema, targetSchema);
        MigrationStrategy strategy = new LoggingStrategyWrapper(new DefaultMigrationStrategy());
        strategy.setSourceSchema(sourceSchema);
        strategy.setTargetSchema(targetSchema);
        diffExtractor.process(strategy);
        strategy.apply(sink);
        sink.commitChanges();
    }

    private Schema filter(Schema schema) {
        Schema filteredSchema = new Schema();
        filteredSchema.setTables(schema.getTables().stream().filter(this::isNameIncluded).collect(toList()));
        filteredSchema.setViews(schema.getViews().stream().filter(this::isNameIncluded).collect(toList()));
        filteredSchema.setSequences(schema.getSequences().stream().filter(this::isNameIncluded).collect(toList()));
        filteredSchema.setEnums(schema.getEnums().stream().filter(this::isNameIncluded).collect(toList()));

        return filteredSchema;
    }

    private boolean isNameIncluded(SchemaItem item) {
        if (configuration.getObjectNameExcludeList() != null && configuration.getObjectNameExcludeList().contains(item.getName())) {
            return false;
        }
        if (configuration.getObjectNameIncludeList() == null || configuration.getObjectNameIncludeList().contains(item.getName())) {
            return true;
        }
        return false;
    }

    public void setConfiguration(MigrationConfiguration configuration) {
        this.configuration = configuration;
    }
}
