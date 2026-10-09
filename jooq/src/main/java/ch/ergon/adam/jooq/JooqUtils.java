package ch.ergon.adam.jooq;

import ch.ergon.adam.core.db.interfaces.SourceAndSinkAdapter;
import org.jooq.*;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class JooqUtils {

    private JooqUtils() {
        throw new UnsupportedOperationException();
    }

    public static Meta extractMeta(DSLContext context, String schemaName) {
        if (schemaName == null) {
            return context.meta();
        }
        List<Schema> schemas = context.meta().getSchemas(schemaName);
        if (schemas.isEmpty()) {
            String knownSchemas = context.meta().getSchemas().stream().map(Named::getName).collect(Collectors.joining(","));
            throw new RuntimeException("Schema [" + schemaName + "] not found. Known schemas are [" + knownSchemas + "]");
        }
        return context.meta(schemas.get(0));
    }

    public static SQLDialect getSqlDialect(String url, SQLDialect defaultDialect) {
        String dialect = extractDialect(url);
        if (dialect == null) {
            return defaultDialect;
        }
        return SQLDialect.valueOf(dialect);
    }

    private static String extractDialect(String url) {
        int idx = url.indexOf("dialect=");
        if (idx < 0) {
            return null;
        }
        idx += "dialect=".length();
        int endIdx = url.indexOf("&", idx);
        if (endIdx < 0) {
            return url.substring(idx);
        }
        return url.substring(idx, endIdx);
    }

    public static String ensureCorrectEscaping(String statement, SQLDialect dialect) {
        if (Objects.requireNonNull(dialect) == SQLDialect.MARIADB) {
            return statement.replace("\"", "`");
        }
        return statement;
    }
}
