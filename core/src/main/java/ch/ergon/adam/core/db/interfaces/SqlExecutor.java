package ch.ergon.adam.core.db.interfaces;

public interface SqlExecutor extends AutoCloseable {
    void executeScript(String script);

    Object queryResult(String query, Object... params);

    void rollback();

    void dropSchema();
}
