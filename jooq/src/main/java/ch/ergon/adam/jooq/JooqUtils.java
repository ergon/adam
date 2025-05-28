package ch.ergon.adam.jooq;

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

    public static String ensureCorrectEscaping(String statement, SQLDialect dialect) {
        if (Objects.requireNonNull(dialect) == SQLDialect.MARIADB) {
            return statement.replace("\"", "`");
        }
        return statement;
    }
}
