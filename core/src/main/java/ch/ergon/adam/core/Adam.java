package ch.ergon.adam.core;

import ch.ergon.adam.core.db.SchemaMigrator;
import ch.ergon.adam.core.db.SourceAndSinkFactory;
import ch.ergon.adam.core.db.interfaces.SqlExecutor;
import ch.ergon.adam.core.filetree.ClasspathTraverser;
import ch.ergon.adam.core.filetree.DirectoryTraverser;
import ch.ergon.adam.core.filetree.FileTreeTraverser;
import ch.ergon.adam.core.filetree.TraverserFile;
import ch.ergon.adam.core.prepost.GitVersionTree;
import ch.ergon.adam.core.prepost.MigrationScriptProvider;
import ch.ergon.adam.core.prepost.MigrationStep;
import ch.ergon.adam.core.prepost.MigrationStepExecutor;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static ch.ergon.adam.core.prepost.MigrationStep.POSTMIGRATION_ALWAYS;
import static ch.ergon.adam.core.prepost.MigrationStep.POSTMIGRATION_INIT;
import static ch.ergon.adam.core.prepost.MigrationStep.POSTMIGRATION_ONCE;
import static ch.ergon.adam.core.prepost.MigrationStep.PREMIGRATION_ALWAYS;
import static ch.ergon.adam.core.prepost.MigrationStep.PREMIGRATION_INIT;
import static ch.ergon.adam.core.prepost.MigrationStep.PREMIGRATION_ONCE;
import static ch.ergon.adam.core.prepost.db_schema_version.DbSchemaVersionSource.SCHEMA_VERSION_TABLE_NAME;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static java.lang.ClassLoader.getSystemResourceAsStream;
import static java.lang.String.format;

public class Adam {

    private static final Logger logger = LoggerFactory.getLogger(Adam.class);
    public static final String TARGET_VERSION_FILE_NAME = "target_version";
    public static final String HISTORY_FILE_NAME = "git_history";
    public static final String DEFAULT_ADAM_PACKAGE = "adam/";
    public static final String DEFAULT_SCHEMA_PACKAGE = DEFAULT_ADAM_PACKAGE + "schema";
    public static final String DEFAULT_SCRIPTS_PACKAGE = DEFAULT_ADAM_PACKAGE + "scripts";
    public static final String DEFAULT_ADAM_EXPORT_PACKAGE = DEFAULT_ADAM_PACKAGE + "export";
    public static final String DEFAULT_MAIN_RESOURCE_PATH = "src/main/resources/";

    private final String referenceUrl;
    private final String targetUrl;
    private final String targetVersion;
    private final GitVersionTree versionTree;
    private final FileTreeTraverser migrationScriptTraverser;
    private boolean allowUnknownDBVersion = false;
    private boolean allowNonForwardMigration = false;
    private boolean migrateSameVersion = false;
    private Collection<String> includes;
    private Collection<String> excludes;


    public static Adam usingGitRepo(String referenceSchemaUrl, String targetUrl, String targetVersion, File migrationScriptPath, File gitRepo) throws IOException {
        return new Adam(referenceSchemaUrl, targetUrl, targetVersion, new GitVersionTree(gitRepo.toPath()), new DirectoryTraverser(migrationScriptPath.toPath()));
    }

    public static Adam usingExportDirectory(String targetUrl, String referenceSchemaProtocol, Path schemaSourcePath, Path exportPath) throws IOException {
        String referenceSchemaUrl = referenceSchemaProtocol + "://" + schemaSourcePath;
        return new Adam(
            referenceSchemaUrl,
            targetUrl,
            readTargetVersionFromFile(exportPath),
            getGitVersionTreeFromFile(exportPath),
            new DirectoryTraverser(exportPath.resolve("scripts")));
    }

    public static Adam usingClasspath(String targetUrl, String referenceSchemaProtocol) throws IOException {
        String schemaSourcePathPackage = System.getProperty("adam.schema.package", DEFAULT_SCHEMA_PACKAGE);
        String exportPackage = System.getProperty("adam.export.package", DEFAULT_ADAM_EXPORT_PACKAGE);
        return usingClasspath(targetUrl, referenceSchemaProtocol, schemaSourcePathPackage, exportPackage);
    }

    public static Adam usingClasspath(String targetUrl, String referenceSchemaProtocol, String schemaSourcePackage, String exportPackage) throws IOException {
        String referenceSchemaUrl = referenceSchemaProtocol + "-classpath://" + schemaSourcePackage;

        return new Adam(
            referenceSchemaUrl,
            targetUrl,
            readTargetVersionFromClasspath(exportPackage),
            getGitVersionTreeFromClasspath(exportPackage),
            new ClasspathTraverser(exportPackage + "/scripts"));
    }

    private static GitVersionTree getGitVersionTreeFromFile(Path exportPath) throws IOException {
        Path historyFile = exportPath.resolve(HISTORY_FILE_NAME);
        try (InputStream is = new FileInputStream(historyFile.toFile())) {
            return new GitVersionTree(is);
        }
    }

    private static String readTargetVersionFromFile(Path exportPath) throws IOException {
        Path targetVersionFile = exportPath.resolve(TARGET_VERSION_FILE_NAME);
        return Files.readAllLines(targetVersionFile).get(0);
    }

    private static GitVersionTree getGitVersionTreeFromClasspath(String exportPackage) throws IOException {
        String historyFile = exportPackage + "/" + HISTORY_FILE_NAME;
        try (InputStream is = getSystemResourceAsStream(historyFile)) {
            return new GitVersionTree(is);
        }
    }

    private static String readTargetVersionFromClasspath(String exportPackage) throws IOException {
        String targetVersionFile = exportPackage + "/" + TARGET_VERSION_FILE_NAME;
        try (InputStream is = getSystemResourceAsStream(targetVersionFile);
             InputStreamReader reader = new InputStreamReader(is);
             BufferedReader bufferedReader = new BufferedReader(reader)) {
            return bufferedReader.readLine();
        }
    }


    private Adam(String referenceUrl, String targetUrl, String targetVersion, GitVersionTree versionTree, FileTreeTraverser migrationScriptTraverser) {
        this.referenceUrl = referenceUrl;
        this.targetUrl = targetUrl;
        this.targetVersion = targetVersion;
        this.versionTree = versionTree;
        this.migrationScriptTraverser = migrationScriptTraverser;

        if (!versionTree.isKnownVersion(targetVersion)) {
            throw new RuntimeException("Target version [" + targetVersion + "] is unknown.");
        }
    }

    public void execute() throws IOException {
        logger.info("Execute migration: referenceUrl '{}', targetUrl '{}', targetVersion '{}'", referenceUrl, targetUrl, targetVersion);

        ensureSchemaVersionTable(targetUrl);
        try (SqlExecutor targetExecutor = SourceAndSinkFactory.getInstance().getSqlExecutor(targetUrl)) {
            try {
                ensureNoInProgressMigrations(targetExecutor);
                String fromVersion = getDbSchemaVersion(targetExecutor);
                boolean isDbInit = fromVersion == null;
                if (isDbInit) {
                    logger.info("Doing initial db migration to '{}'. Executing init instead of pre-/post-migration scripts", targetVersion);
                } else {
                    logger.info("Doing db migration from '{}' to '{}'.", fromVersion, targetVersion);
                }
                if (!isDbInit && fromVersion.equals(targetVersion) && !migrateSameVersion) {
                    logger.info("Skip migration since source- and targetVersion are the same.");
                    return;
                }
                createSchemaVersionEntry(targetExecutor, fromVersion, targetVersion);

                boolean unknownVersion = !isDbInit && !versionTree.isKnownVersion(fromVersion);
                if (unknownVersion) {
                    if (!allowUnknownDBVersion) {
                        throw new RuntimeException("Current db version [" + fromVersion + "] is unknown.");
                    }
                    logger.warn("The current db schema version [{}] is unknown. Migration will be executed WITHOUT pre-/post-migration.", fromVersion);
                } else if (fromVersion != null && !versionTree.isAncestor(fromVersion, targetVersion)) {
                    if (!allowNonForwardMigration) {
                        throw new RuntimeException("DB version [" + fromVersion + "] is not an ancestor of target version [" + targetVersion + "]");
                    }
                    logger.warn("DB version [" + fromVersion + "] is not an ancestor of target version [" + targetVersion + "]");
                }

                if (!unknownVersion && !isDbInit) {
                    List<String> versions = versionTree.getVersionsBetween(fromVersion, targetVersion);
                    MigrationScriptProvider migrationScriptProvider = new MigrationScriptProvider(migrationScriptTraverser, versions);
                    logExecutionOrder(migrationScriptProvider);
                    MigrationStepExecutor executor = new MigrationStepExecutor(migrationScriptProvider, targetExecutor);
                    executor.executeStep(PREMIGRATION_ALWAYS);
                    executor.executeStep(PREMIGRATION_ONCE);
                    SchemaMigrator.migrate(referenceUrl, targetUrl, getMigrationConfig());
                    executor.executeStep(POSTMIGRATION_ONCE);
                    executor.executeStep(POSTMIGRATION_ALWAYS);
                } else {
                    MigrationScriptProvider migrationScriptProvider = new MigrationScriptProvider(migrationScriptTraverser);
                    MigrationStepExecutor executor = new MigrationStepExecutor(migrationScriptProvider, targetExecutor);
                    if (isDbInit) {
                        executor.executeStep(PREMIGRATION_INIT);
                    }
                    SchemaMigrator.migrate(referenceUrl, targetUrl, getMigrationConfig());
                    if (isDbInit) {
                        executor.executeStep(POSTMIGRATION_INIT);
                    }
                }
                completeSchemaVersionEntry(targetExecutor);
            } catch (Exception e) {
                targetExecutor.rollback();
                throw e;
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    private MigrationConfiguration getMigrationConfig() {
        MigrationConfiguration migrationConfiguration = new MigrationConfiguration();
        Set<String> excludeList = newHashSet(SCHEMA_VERSION_TABLE_NAME);
        if (excludes != null) {
            excludeList.addAll(excludes);
        }
        migrationConfiguration.setObjectNameExcludeList(excludeList);
        migrationConfiguration.setObjectNameIncludeList(includes);
        return migrationConfiguration;
    }

    private void logExecutionOrder(MigrationScriptProvider migrationScriptProvider) {
        logger.info("The following scripts will be executed in given order:");
        logExecutionOrderForStep(migrationScriptProvider, PREMIGRATION_ALWAYS);
        logExecutionOrderForStep(migrationScriptProvider, PREMIGRATION_ONCE);
        logger.info("Automated schema migration.");
        logExecutionOrderForStep(migrationScriptProvider, POSTMIGRATION_ONCE);
        logExecutionOrderForStep(migrationScriptProvider, POSTMIGRATION_ALWAYS);
    }

    private void logExecutionOrderForStep(MigrationScriptProvider migrationScriptProvider, MigrationStep step) {
        List<TraverserFile> scripts = migrationScriptProvider.getMigrationScripts(step);
        if (scripts.isEmpty()) {
            return;
        }
        logger.info("Step {}:", step.name());
        scripts.forEach(script -> {
            logger.info(" - {}", script.getName());
        });
    }

    private void ensureSchemaVersionTable(String targetUrl) {
        String schemaPath = "dbschemaversion://dummy";
        MigrationConfiguration migrationConfiguration = new MigrationConfiguration();
        migrationConfiguration.setObjectNameExcludeList(null);
        migrationConfiguration.setObjectNameIncludeList(newArrayList(SCHEMA_VERSION_TABLE_NAME));
        SchemaMigrator.migrate(schemaPath, targetUrl, migrationConfiguration);
    }

    private void ensureNoInProgressMigrations(SqlExecutor sqlExecutor) {
        Object result = sqlExecutor.queryResult(format("SELECT COUNT(1) FROM \"%s\" WHERE \"execution_completed_at\" IS NULL", SCHEMA_VERSION_TABLE_NAME));
        if (Integer.parseInt(result.toString()) != 0) {
            throw new RuntimeException("There is an unfinished migration in [" + SCHEMA_VERSION_TABLE_NAME + "]");
        }
    }

    private String getDbSchemaVersion(SqlExecutor sqlExecutor) {
        Object result = sqlExecutor.queryResult(format("SELECT \"target_version\" FROM \"%s\" ORDER BY \"execution_started_at\" DESC", SCHEMA_VERSION_TABLE_NAME));
        return result == null ? null : result.toString();
    }

    private void createSchemaVersionEntry(SqlExecutor sqlExecutor, String fromVersion, String toVersion) {
        sqlExecutor.queryResult(format("INSERT INTO \"%s\" (\"execution_started_at\", \"source_version\", \"target_version\") VALUES (CURRENT_TIMESTAMP, ?, ?)", SCHEMA_VERSION_TABLE_NAME), fromVersion, toVersion);
    }

    private void completeSchemaVersionEntry(SqlExecutor sqlExecutor) {
        sqlExecutor.queryResult(format("UPDATE \"%s\" SET \"execution_completed_at\" = CURRENT_TIMESTAMP WHERE \"execution_completed_at\" IS NULL", SCHEMA_VERSION_TABLE_NAME));
    }

    public void setAllowUnknownDBVersion(boolean allowUnknownDBVersion) {
        this.allowUnknownDBVersion = allowUnknownDBVersion;
    }

    public boolean isAllowUnknownDBVersion() {
        return allowUnknownDBVersion;
    }

    public boolean isMigrateSameVersion() {
        return migrateSameVersion;
    }

    public void setMigrateSameVersion(boolean migrateSameVersion) {
        this.migrateSameVersion = migrateSameVersion;
    }

    public boolean isAllowNonForwardMigration() {
        return allowNonForwardMigration;
    }

    public void setAllowNonForwardMigration(boolean allowNonForwardMigration) {
        this.allowNonForwardMigration = allowNonForwardMigration;
    }

    public void setIncludes(Collection<String> includes) {
        this.includes = includes;
    }

    public void setExcludes(Collection<String> excludes) {
        this.excludes = excludes;
    }
}
