package ch.ergon.adam.gradleplugin.tasks;

import ch.ergon.adam.core.filetree.DirectoryTraverser;
import ch.ergon.adam.core.prepost.MigrationScriptProvider;
import ch.ergon.adam.gradleplugin.AdamExtension;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.*;
import org.gradle.work.DisableCachingByDefault;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;

import static com.google.common.base.MoreObjects.firstNonNull;

@DisableCachingByDefault(because = "Output depends on the git history")
public class ExportMigrationScriptsTask extends DefaultTask {

    private final AdamExtension extension;
    @Internal
    private Path gitRepo;
    private Path migrationScriptSourcePath;
    private Path migrationScriptExportPath;

    public ExportMigrationScriptsTask() {
        extension = getProject().getExtensions().getByType(AdamExtension.class);
    }

    @TaskAction
    void exportMigrationScripts() throws IOException {
        getLogger().info("Export migration scripts from [{}] to [{}] using git repo [{}].",
            getMigrationScriptSourcePath().toAbsolutePath(),
            getMigrationScriptExportPath().toAbsolutePath(),
            getGitRepo().toAbsolutePath());
        deleteDirectoryRecursion(getMigrationScriptExportPath());
        MigrationScriptProvider scriptProvider = new MigrationScriptProvider(new DirectoryTraverser(getMigrationScriptSourcePath()), getGitRepo());
        scriptProvider.exportMigrationScripts(getMigrationScriptExportPath().toFile());
    }

    void deleteDirectoryRecursion(Path path) throws IOException {
        if (Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)) {
            try (DirectoryStream<Path> entries = Files.newDirectoryStream(path)) {
                for (Path entry : entries) {
                    deleteDirectoryRecursion(entry);
                }
            }
        }
        Files.delete(path);
    }

    @OutputDirectory
    public Path getMigrationScriptExportPath() {
        return firstNonNull(migrationScriptExportPath, extension.getMigrationScriptsExportPath());
    }

    public void setMigrationScriptExportPath(Path migrationScriptExportPath) {
        this.migrationScriptExportPath = migrationScriptExportPath;
    }

    @InputDirectory
    @PathSensitive(PathSensitivity.RELATIVE)
    public Path getRefsPath() {
        return firstNonNull(gitRepo, extension.gitRepo).resolve(".git/refs");
    }

    public Path getGitRepo() {
        return firstNonNull(gitRepo, extension.gitRepo);
    }

    public void setGitRepo(Path gitRepo) {
        this.gitRepo = gitRepo;
    }

    @InputDirectory
    @SkipWhenEmpty
    @PathSensitive(PathSensitivity.RELATIVE)
    public Path getMigrationScriptSourcePath() {
        return firstNonNull(migrationScriptSourcePath, extension.getMigrationScriptsSourcePath());
    }

    public void setMigrationScriptSourcePath(Path migrationScriptSourcePath) {
        this.migrationScriptSourcePath = migrationScriptSourcePath;
    }

}
