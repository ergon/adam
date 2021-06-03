package ch.ergon.adam.gradleplugin.tasks;

import ch.ergon.adam.gradleplugin.adamExtension;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.google.common.base.MoreObjects.firstNonNull;
import static java.nio.charset.StandardCharsets.UTF_8;

public class ExportTargetVersionTask extends DefaultTask {

    private final adamExtension extension;
    private Path targetVersionFile;
    private String targetVersion;

    public ExportTargetVersionTask() {
        extension = getProject().getExtensions().getByType(adamExtension.class);
    }


    @TaskAction
    void exportTargetVersion() throws IOException {
        getLogger().info("Export target version to [{}].", getTargetVersionFile().toAbsolutePath());
        Files.write(getTargetVersionFile(), getTargetVersion().getBytes(UTF_8));
    }

    @OutputFile
    public Path getTargetVersionFile() {
        return firstNonNull(targetVersionFile, extension.getTargetVersionExportFile());
    }

    public void setTargetVersionFile(Path targetVersionFile) {
        this.targetVersionFile = targetVersionFile;
    }

    @Input
    public String getTargetVersion() {
        return firstNonNull(targetVersion, extension.targetVersion);
    }

    public void setTargetVersion(String targetVersion) {
        this.targetVersion = targetVersion;
    }
}
