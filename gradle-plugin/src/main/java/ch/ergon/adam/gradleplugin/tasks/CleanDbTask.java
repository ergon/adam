package ch.ergon.adam.gradleplugin.tasks;

import ch.ergon.adam.core.db.SourceAndSinkFactory;
import ch.ergon.adam.core.db.interfaces.SqlExecutor;
import ch.ergon.adam.gradleplugin.adamExtension;
import org.gradle.api.DefaultTask;
import org.gradle.api.InvalidUserDataException;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Strings.isNullOrEmpty;

public class CleanDbTask extends DefaultTask {

    private final adamExtension extension;
    private String targetUrl;

    public CleanDbTask() {
        extension = getProject().getExtensions().getByType(adamExtension.class);
    }

    @TaskAction
    void cleanDb() {
        if (isNullOrEmpty(getTargetUrl())) {
            throw new InvalidUserDataException("The property targetUrl needs to be set.");
        }

        try (SqlExecutor sqlExecutor = SourceAndSinkFactory.getInstance().getSqlExecutor(getTargetUrl())) {
            sqlExecutor.dropSchema();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Input
    public String getTargetUrl() {
        return firstNonNull(targetUrl, extension.targetUrl);
    }

    public void setTargetUrl(String targetUrl) {
        this.targetUrl = targetUrl;
    }
}
