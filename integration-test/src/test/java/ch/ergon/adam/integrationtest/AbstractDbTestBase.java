package ch.ergon.adam.integrationtest;

import ch.ergon.adam.yml.YmlSink;
import ch.ergon.adam.core.db.SchemaMigrator;
import ch.ergon.adam.core.db.SourceAndSinkFactory;
import ch.ergon.adam.core.db.interfaces.SchemaSink;
import ch.ergon.adam.core.db.interfaces.SchemaSource;
import ch.ergon.adam.core.db.schema.Schema;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.io.ByteArrayOutputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractDbTestBase {


    private final TestDbUrlProvider testDbUrlProvider;
    private List<AutoCloseable> closeables = new ArrayList<>();

    public AbstractDbTestBase(TestDbUrlProvider testDbUrlProvider) {
        this.testDbUrlProvider = testDbUrlProvider;
    }

    @BeforeEach
    public void init() throws Exception {
        testDbUrlProvider.initDbForTest();
    }

    @AfterEach
    public void cleanupCloseables() {
        closeables.forEach(connection -> {
            try {
                connection.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        closeables.clear();
        TestDbUrlProvider.close();
    }

    protected Connection getSourceDbConnection() throws SQLException {
        return testDbUrlProvider.getSourceDbConnection();
    }

    protected Connection getTargetDbConnection() throws SQLException {
        return testDbUrlProvider.getTargetDbConnection();
    }
    protected SchemaSink getTargetDbSink() {
        SchemaSink sink = SourceAndSinkFactory.getInstance().getSink(getTargetDbUrl());
        closeables.add(sink);
        return sink;
    }

    protected String getTargetDbUrl() {
        return testDbUrlProvider.getTargetDbUrl();
    }

    protected SchemaSource getSourceDbSource() {
        SchemaSource source = SourceAndSinkFactory.getInstance().getSource(getSourceDbUrl());
        closeables.add(source);
        return source;
    }

    protected String getSourceDbUrl() {
        return testDbUrlProvider.getSourceDbUrl();
    }

    protected SchemaSource getTargetDbSource() {
        SchemaSource source = SourceAndSinkFactory.getInstance().getSource(getTargetDbUrl());
        closeables.add(source);
        return source;
    }

    protected DummySink dumpToDummySink(SchemaSource targetDbSource) {
        DummySink dummySink = new DummySink();
        new SchemaMigrator(new EmptySource(), targetDbSource, dummySink).migrate();
        return dummySink;
    }

    protected void sourceToTarget() {
        new SchemaMigrator(getTargetDbSource(), getSourceDbSource(), getTargetDbSink()).migrate();
    }

    protected DummySink targetToDummy() {
        return toDummy(getTargetDbSource());
    }

    private DummySink toDummy(SchemaSource source) {
        DummySink dummySink = new DummySink();
        new SchemaMigrator(new EmptySource(), source, dummySink).migrate();
        return dummySink;
    }


    protected void migrateTargetWithSchema(Schema schema) {
        new SchemaMigrator(getTargetDbSource(), new StubSchemaSource(schema), getTargetDbSink()).migrate();
    }


    protected String targetToYml() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        YmlSink ymlSink = new YmlSink(baos);
        new SchemaMigrator(new EmptySource(), getTargetDbSource(), ymlSink).migrate();
        return baos.toString();
    }


}
