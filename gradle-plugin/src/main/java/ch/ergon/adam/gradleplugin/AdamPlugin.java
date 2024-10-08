package ch.ergon.adam.gradleplugin;

import javax.annotation.Nonnull;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;

import ch.ergon.adam.gradleplugin.tasks.CleanDbTask;
import ch.ergon.adam.gradleplugin.tasks.ExportGitHistoryTask;
import ch.ergon.adam.gradleplugin.tasks.ExportMigrationScriptsTask;
import ch.ergon.adam.gradleplugin.tasks.ExportTargetVersionTask;
import ch.ergon.adam.gradleplugin.tasks.GenerateJooqMetamodelTask;
import ch.ergon.adam.gradleplugin.tasks.MigrateDBTask;

public class AdamPlugin implements Plugin<Project> {

	public static final String ADAM_EXTENSION = "adam";

	@Override
	public void apply(@Nonnull Project project) {

		project.getExtensions().create(ADAM_EXTENSION, AdamExtension.class, project);

		project.getTasks().create("adamExportMigrationScripts", ExportMigrationScriptsTask.class)
				.setGroup(ADAM_EXTENSION);

		project.getTasks().create("adamExportGitHistory", ExportGitHistoryTask.class).setGroup(ADAM_EXTENSION);

		project.getTasks().create("adamExportTargetVersion", ExportTargetVersionTask.class).setGroup(ADAM_EXTENSION);

		project.getTasks().create("adamGenerateJooqMetamodel", GenerateJooqMetamodelTask.class)
				.setGroup(ADAM_EXTENSION);

		Task adamExportTask = project.getTasks().create("adamExport", Task.class);
		adamExportTask.setGroup(ADAM_EXTENSION);
		adamExportTask.dependsOn("adamExportTargetVersion", "adamExportGitHistory", "adamExportMigrationScripts");

		Task migrateDbTask = project.getTasks().create("adamMigrateDb", MigrateDBTask.class);
		migrateDbTask.setGroup(ADAM_EXTENSION);
		migrateDbTask.dependsOn(adamExportTask);

		project.getTasks().create("adamCleanDb", CleanDbTask.class).setGroup(ADAM_EXTENSION);
	}
}
