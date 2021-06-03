package ch.ergon.adam.core.db;

import ch.ergon.adam.core.db.interfaces.SchemaSink;
import ch.ergon.adam.core.db.interfaces.SchemaSource;
import ch.ergon.adam.core.db.interfaces.SourceAndSinkAdapter;
import ch.ergon.adam.core.db.interfaces.SqlExecutor;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static java.util.stream.Collectors.toList;

public class SourceAndSinkFactory {

    private final Logger logger = LoggerFactory.getLogger(SourceAndSinkFactory.class);

    private static SourceAndSinkFactory instance;
    private final List<SourceAndSinkAdapter> adapters;

    public static SourceAndSinkFactory getInstance() {
        if (instance == null) {
            createInstance();
        }
        return instance;
    }

    private static synchronized void createInstance() {
        if (instance == null) {
            instance = new SourceAndSinkFactory();
        }
    }

    private SourceAndSinkFactory() {
        Reflections reflections = new Reflections("ch.ergon.adam");
        Set<Class<? extends SourceAndSinkAdapter>> adapterTypes = reflections.getSubTypesOf(SourceAndSinkAdapter.class);
        adapters = adapterTypes.stream().map(this::createAdapterInstance).filter(Objects::nonNull).collect(toList());
        adapters.forEach(adapter -> {
            logger.debug("New migration adapter registered [" + adapter.getClass().getName() + "]");
        });

    }

    private SourceAndSinkAdapter createAdapterInstance(Class<? extends SourceAndSinkAdapter> adapterClass) {
        try {
            Constructor<? extends SourceAndSinkAdapter> constructor = adapterClass.getConstructor();
            return constructor.newInstance();
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            logger.warn("Could not create adapter factory [" + adapterClass.getName() + "]", e);
            return null;
        }
    }

    public SchemaSource getSource(String url) {
        return getAdapterForUrl(url).createSource(url);
    }

    public SchemaSink getSink(String url) {
        return getAdapterForUrl(url).createSink(url);
    }

    public SqlExecutor getSqlExecutor(String url) {
        return getAdapterForUrl(url).createSqlExecutor(url);
    }

    private SourceAndSinkAdapter getAdapterForUrl(String url) {
        List<SourceAndSinkAdapter> supportedAdapters = adapters.stream().filter(adapter -> adapter.supportsUrl(url)).collect(toList());
        if (supportedAdapters.isEmpty()) {
            throw new RuntimeException("Could not find migration adapter for url [" + url + "]");
        } else if (supportedAdapters.size() > 1) {
            logger.warn("Found multiple migration adapters for url [" + url + "]:");
            supportedAdapters.forEach(adapter -> logger.warn("\t" + adapter.getClass().getName()));
        }
        SourceAndSinkAdapter selectedAdapter = supportedAdapters.get(0);
        logger.debug("Using migration adapter [" + selectedAdapter.getClass().getName() + "] for url [" + url + "]:");
        return selectedAdapter;
    }

}
