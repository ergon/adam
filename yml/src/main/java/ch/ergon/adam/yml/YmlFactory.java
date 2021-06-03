package ch.ergon.adam.yml;

import ch.ergon.adam.core.db.interfaces.SchemaSink;
import ch.ergon.adam.core.db.interfaces.SchemaSource;
import ch.ergon.adam.core.db.interfaces.SourceAndSinkAdapter;
import ch.ergon.adam.core.db.interfaces.SqlExecutor;
import ch.ergon.adam.core.filetree.ClasspathTraverser;
import ch.ergon.adam.core.filetree.DirectoryTraverser;
import ch.ergon.adam.core.filetree.FileTreeTraverser;

import java.io.File;
import java.nio.file.Paths;

public class YmlFactory implements SourceAndSinkAdapter {
    @Override
    public boolean supportsUrl(String url) {
        return url.toLowerCase().startsWith("yml://") || url.toLowerCase().startsWith("yml-classpath://");
    }

    @Override
    public SchemaSource createSource(String url) {
        return new YmlSource(getFileTraverser(url));
    }

    @Override
    public SchemaSink createSink(String url) {
        if (url.toLowerCase().startsWith("yml-classpath://")) {
            throw new RuntimeException("Cannot create a sink to 'yml-classpath://'");
        }
        return new YmlSink(new File(url.replaceFirst("(?i)yml://", "")));
    }

    @Override
    public SqlExecutor createSqlExecutor(String url) {
        throw new RuntimeException("Not implemented");
    }

    private FileTreeTraverser getFileTraverser(String url) {
        if (url.toLowerCase().startsWith("yml://")) {
            String urlPath = url.replaceFirst("(?i)yml://", "");
            return new DirectoryTraverser(Paths.get(urlPath));
        } else if (url.toLowerCase().startsWith("yml-classpath://")) {
            String urlPath = url.replaceFirst("yml-classpath://", "");
            return new ClasspathTraverser(urlPath);
        } else {
            throw new RuntimeException("Unsupported url [" + url + "]");
        }
    }
}
