package ch.ergon.adam.gradleplugin.tasks;

import ch.ergon.adam.core.Adam;
import ch.ergon.adam.gradleplugin.adamExtension;
import org.gradle.api.DefaultTask;
import org.gradle.api.InvalidUserDataException;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;

import java.io.IOException;
import java.nio.file.Path;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Strings.isNullOrEmpty;

public class MigrateDBTask extends DefaultTask {

    private String targetUrl;
    private Path migrationExportPath;
    private Path schemaSourcePath;
    private String schemaSourceProtocol;
    private boolean allowUnknownDBVersion;
    private boolean migrateSameVersion;
    private boolean allowNonForwardMigration;
    private adamExtension extension;


    public MigrateDBTask() {
        extension = getProject().getExtensions().getByType(adamExtension.class);
    }

    @TaskAction
    void migrateDb() throws IOException {

        if (isNullOrEmpty(getTargetUrl())) {
            throw new InvalidUserDataException("The property targetUrl needs to be set.");
        }


        Adam adam = Adam.usingExportDirectory(
            getTargetUrl(),
            getSchemaSourceProtocol(),
            getSchemaSourcePath(),
            getMigrationExportPath()
        );
        adam.setAllowUnknownDBVersion(allowUnknownDBVersion);
        adam.setMigrateSameVersion(migrateSameVersion);
        adam.setAllowNonForwardMigration(allowNonForwardMigration);
        adam.execute();
    }

    @Input
    public String getTargetUrl() {
        return firstNonNull(targetUrl, extension.targetUrl);
    }

    public void setTargetUrl(String targetUrl) {
        this.targetUrl = targetUrl;
    }

    @InputDirectory
    @Optional
    public Path getMigrationExportPath() {
        return firstNonNull(migrationExportPath, extension.getAdamResourceExportPath());
    }

    public void setMigrationExportPath(Path migrationExportPath) {
        this.migrationExportPath = migrationExportPath;
    }

    @InputDirectory
    public Path getSchemaSourcePath() {
        return firstNonNull(schemaSourcePath, extension.getSchemaSourcePath());
    }

    public void setSchemaSourcePath(Path schemaSourcePath) {
        this.schemaSourcePath = schemaSourcePath;
    }

    @Input
    public boolean isAllowUnknownDBVersion() {
        return allowUnknownDBVersion;
    }

    public void setAllowUnknownDBVersion(boolean allowUnknownDBVersion) {
        this.allowUnknownDBVersion = allowUnknownDBVersion;
    }

    @Input
    public boolean isMigrateSameVersion() {
        return migrateSameVersion;
    }

    public void setMigrateSameVersion(boolean migrateSameVersion) {
        this.migrateSameVersion = migrateSameVersion;
    }

    @Input
    public String getSchemaSourceProtocol() {
        return firstNonNull(schemaSourceProtocol, extension.schemaSourceProtocol);
    }

    public void setSchemaSourceProtocol(String schemaSourceProtocol) {
        this.schemaSourceProtocol = schemaSourceProtocol;
    }

    @Input
    public boolean getAllowNonForwardMigration() {
        return allowNonForwardMigration;
    }

    public void setAllowNonForwardMigration(boolean allowNonForwardMigration) {
        this.allowNonForwardMigration = allowNonForwardMigration;
    }
}
