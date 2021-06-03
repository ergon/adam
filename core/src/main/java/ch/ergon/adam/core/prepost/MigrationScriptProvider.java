package ch.ergon.adam.core.prepost;

import ch.ergon.adam.core.filetree.FileTreeTraverser;
import ch.ergon.adam.core.filetree.TraverserFile;
import com.google.common.io.Files;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;

public class MigrationScriptProvider {

    private static final String VERSION_PREFIX = "-- commit:";
    private static final String SQL = ".sql";
    private static final Logger logger = LoggerFactory.getLogger(MigrationScriptProvider.class);
    private final FileTreeTraverser migrationScriptDirectoryTraverser;
    private final Path gitRepo;
    private final List<String> versions;
    private Git git;

    public MigrationScriptProvider(FileTreeTraverser migrationScriptDirectoryTraverser) {
        this.migrationScriptDirectoryTraverser = migrationScriptDirectoryTraverser;
        this.versions = null;
        this.gitRepo = null;
    }

    public MigrationScriptProvider(FileTreeTraverser migrationScriptDirectoryTraverser, Path gitRepo) {
        this.migrationScriptDirectoryTraverser = migrationScriptDirectoryTraverser;
        this.gitRepo = gitRepo;
        this.versions = null;
    }

    public MigrationScriptProvider(FileTreeTraverser migrationScriptDirectoryTraverser, List<String> versions) {
        this.migrationScriptDirectoryTraverser = migrationScriptDirectoryTraverser;
        this.versions = versions;
        this.gitRepo = null;
    }

    public MigrationScriptProvider(FileTreeTraverser migrationScriptDirectoryTraverser, Path gitRepo, List<String> versions) {
        this.migrationScriptDirectoryTraverser = migrationScriptDirectoryTraverser;
        this.gitRepo = gitRepo;
        this.versions = versions;
    }

    public List<TraverserFile> getMigrationScripts(MigrationStep step) {
        FileTreeTraverser stepTraverser = getTraverserForMigrationStepScripts(step);
        if (stepRequiresVersion(step)) {
            List<ScriptWithVersion> scripts = loadScriptsWithVersion(stepTraverser);
            return scripts.stream()
                .filter(scriptWithVersion -> versions.contains(scriptWithVersion.getVersion()))
                .sorted(comparing(scriptWithVersion -> versions.indexOf(scriptWithVersion.getVersion())))
                .map(ScriptWithVersion::getScript)
                .collect(toList());
        } else {
            return listSqlFiles(stepTraverser);
        }
    }

    public void exportMigrationScripts(File exportPath) throws IOException {
        if (gitRepo == null) {
            throw new IllegalStateException("Git repo is required to export migration files.");
        }
        for (MigrationStep step : MigrationStep.values()) {
            FileTreeTraverser traverser = getTraverserForMigrationStepScripts(step);
            File stepExportPath = new File(exportPath, step.name());
            if (!stepExportPath.exists()) {
                stepExportPath.mkdirs();
            }
            if (stepRequiresVersion(step)) {
                List<ScriptWithVersion> scriptsWithVersion = loadScriptsWithVersion(traverser);
                for (ScriptWithVersion scriptWithVersion : scriptsWithVersion) {
                    File exportFile = new File(stepExportPath, scriptWithVersion.version + "_" + scriptWithVersion.script.getName());
                    Files.copy(scriptWithVersion.script.getFile(), exportFile);
                }
            } else {
                for (TraverserFile script : listSqlFiles(traverser)) {
                    File exportFile = new File(stepExportPath, script.getName());
                    Files.copy(script.getFile(), exportFile);
                }
            }
        }
    }

    private FileTreeTraverser getTraverserForMigrationStepScripts(MigrationStep step) {
        return migrationScriptDirectoryTraverser.cd(step.name());
    }

    private List<TraverserFile> listSqlFiles(FileTreeTraverser traverser) {
        if (traverser == null) {
            return newArrayList();
        }
        return traverser.getFiles().stream()
            .filter(file -> file.getName().toLowerCase().endsWith(SQL))
            .sorted(comparing(TraverserFile::getName))
            .collect(toList());
    }

    private boolean stepRequiresVersion(MigrationStep step) {
        return step == MigrationStep.POSTMIGRATION_ONCE || step == MigrationStep.PREMIGRATION_ONCE;
    }

    private List<ScriptWithVersion> loadScriptsWithVersion(FileTreeTraverser traverser) {
        return listSqlFiles(traverser).stream()
            .map(file -> new ScriptWithVersion(getScriptVersion(file), file))
            .collect(toList());
    }

    private String getScriptVersion(TraverserFile script) {
        String version = getScriptVersionFromFile(script);
        if (version == null && gitRepo != null) {
            version = getScriptVersionFromGit(script);
        }
        if (version == null) {
            version = getScriptVersionFromFileName(script);
        }
        if (version == null) {
            throw new RuntimeException("Could not get commit for script file [" + script.getName() + "]");
        }
        return version;
    }

    private Git getGit() throws IOException {
        if (git == null) {
            git = Git.open(gitRepo.toFile());
        }
        return git;
    }

    private String getScriptVersionFromGit(TraverserFile script) {

        File scriptFile = script.getFile();
        if (scriptFile == null) {
            throw new RuntimeException("Could net get script file for [" + script.getName() + "]");
        }

        try {
            Git git = getGit();
            File relativeScriptPath = gitRepo.relativize(scriptFile.getAbsoluteFile().toPath()).toFile();
            RevCommit firstCommit = null;
            for (RevCommit revCommit : git.log().addPath(relativeScriptPath.getPath().replaceAll("\\\\", "/")).call()) {
                firstCommit = revCommit;
            }
            if (firstCommit == null) {
                logger.warn("Could not get script version for file [" + relativeScriptPath + "].");
                return null;
            }
            return firstCommit.getId().name();
        } catch (IOException | GitAPIException e) {
            throw new RuntimeException(e);
        }
    }

    private String getScriptVersionFromFileName(TraverserFile script) {
        String fileName = script.getName();
        int indexOfDash = fileName.indexOf("_");
        if (indexOfDash <= 0) {
            return null;
        }
        return fileName.substring(0, indexOfDash);
    }

    private String getScriptVersionFromFile(TraverserFile script) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(script.getInputStream()))) {
            String firstLine = reader.readLine();
            if (firstLine != null && firstLine.startsWith(VERSION_PREFIX)) {
                return firstLine.replaceFirst(VERSION_PREFIX, "").trim();
            }
            return null;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private class ScriptWithVersion {
        private final String version;
        private final TraverserFile script;

        private ScriptWithVersion(String version, TraverserFile script) {
            this.version = version;
            this.script = script;
        }

        public TraverserFile getScript() {
            return script;
        }

        public String getVersion() {
            return version;
        }
    }
}
