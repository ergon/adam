package ch.ergon.adam.gradleplugin;

import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.ObjectId;
import org.gradle.api.Project;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static ch.ergon.adam.core.Adam.DEFAULT_ADAM_EXPORT_PACKAGE;
import static ch.ergon.adam.core.Adam.DEFAULT_ADAM_PACKAGE;
import static ch.ergon.adam.core.Adam.DEFAULT_MAIN_RESOURCE_PATH;
import static ch.ergon.adam.core.Adam.HISTORY_FILE_NAME;
import static ch.ergon.adam.core.Adam.TARGET_VERSION_FILE_NAME;
import static ch.ergon.adam.core.helper.FileHelper.getRepoBase;
import static org.eclipse.jgit.lib.Constants.HEAD;

public class AdamExtension {

    public String targetUrl;
    public String targetVersion;
    public String resourceBuildPath;
    public String resourceSourcePath;
    public String schemaSourceProtocol;
    public String adamExportPackage;
    public String adamSourcePackage;
    public Path gitRepo;

    public AdamExtension(Project project) throws IOException {
        resourceBuildPath = new File(project.getBuildDir(), "resources/main/").getPath();
        resourceSourcePath = project.getProjectDir() + "/" + DEFAULT_MAIN_RESOURCE_PATH;
        adamSourcePackage = DEFAULT_ADAM_PACKAGE;
        adamExportPackage = DEFAULT_ADAM_EXPORT_PACKAGE;
        schemaSourceProtocol = "yml";
        gitRepo = getRepoBase();
        if (gitRepo != null) {
            FileRepository git = new FileRepository(gitRepo.resolve(".git").toFile());
            ObjectId head = git.resolve(HEAD);
            targetVersion = head.name();
        }
    }

    public Path getMigrationScriptsSourcePath() {
        return Paths.get(resourceSourcePath, adamSourcePackage, "scripts");
    }

    public Path getSchemaSourcePath() {
        return Paths.get(resourceSourcePath, adamSourcePackage, "schema");
    }

    public Path getAdamResourceExportPath() {
        return Paths.get(resourceBuildPath, adamExportPackage);
    }

    public Path getHistoryExportFile() {
        return getAdamResourceExportPath().resolve(HISTORY_FILE_NAME);
    }

    public Path getTargetVersionExportFile() {
        return getAdamResourceExportPath().resolve(TARGET_VERSION_FILE_NAME);
    }

    public Path getMigrationScriptsExportPath() {
        return getAdamResourceExportPath().resolve("scripts");
    }
}
