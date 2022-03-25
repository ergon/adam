package ch.ergon.adam.gradleplugin.tasks;

import ch.ergon.adam.core.prepost.GitVersionTree;
import ch.ergon.adam.gradleplugin.AdamExtension;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.google.common.base.MoreObjects.firstNonNull;

public class ExportGitHistoryTask extends DefaultTask {

    private final AdamExtension extension;
    @Internal
    private Path gitRepo;
    private Path historyExportFile;

    public ExportGitHistoryTask() {
        extension = getProject().getExtensions().getByType(AdamExtension.class);
    }

    @TaskAction
    void exportHistory() throws IOException {
        getLogger().info("Export git history to [{}].", getHistoryExportFile().toAbsolutePath());
        GitVersionTree gitVersionTree = new GitVersionTree(getGitRepo());
        Path folder = getHistoryExportFile().getParent();
        Files.createDirectories(folder);
        try (FileOutputStream fos = new FileOutputStream(getHistoryExportFile().toFile())) {
            gitVersionTree.writeToFile(fos);
        }
    }

    @OutputFile
    public Path getHistoryExportFile() {
        return firstNonNull(historyExportFile, extension.getHistoryExportFile());
    }

    public void setHistoryExportFile(Path historyExportFile) {
        this.historyExportFile = historyExportFile;
    }

    @InputDirectory
    public Path getRefsPath() {
        return firstNonNull(gitRepo, extension.gitRepo).resolve(".git/refs");
    }

    public Path getGitRepo() {
        return firstNonNull(gitRepo, extension.gitRepo);
    }

    public void setGitRepo(Path gitRepo) {
        this.gitRepo = gitRepo;
    }
}
