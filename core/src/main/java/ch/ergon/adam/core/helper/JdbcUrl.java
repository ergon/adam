package ch.ergon.adam.core.helper;

public class JdbcUrl {
    public static final String SQLITE_TYPE = "sqlite";

    /**
     * Extracts database type from JDBC URL.
     *
     * @param jdbcUrl the JDBC URL (e.g., "jdbc:mysql://localhost:3306/db" or "jdbc:sqlite:path/to/db")
     * @return the database type (e.g., "mysql", "sqlite", "postgresql") or null if URL is invalid
     */
    public static String getDatabaseTypeFromUrl(String jdbcUrl) {
        if (jdbcUrl == null || !jdbcUrl.startsWith("jdbc:")) {
            return null;
        }

        // Remove "jdbc:" prefix
        String urlWithoutPrefix = jdbcUrl.substring(5);

        // Get everything before the next ":" or "//"
        int colonIndex = urlWithoutPrefix.indexOf(':');
        int slashIndex = urlWithoutPrefix.indexOf("//");

        if (colonIndex == -1 && slashIndex == -1) {
            // Handle cases like "jdbc:sqlite:path"
            return urlWithoutPrefix;
        }

        int endIndex = Math.min(
            colonIndex >= 0 ? colonIndex : Integer.MAX_VALUE,
            slashIndex >= 0 ? slashIndex : Integer.MAX_VALUE
        );
        return urlWithoutPrefix.substring(0, endIndex).toLowerCase();
    }

    public static boolean isDialectUrl(String dialect, String jdbcUrl) {
        return getDatabaseTypeFromUrl(jdbcUrl).equals(dialect);
    }
}
