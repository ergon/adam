package ch.ergon.adam.sqlite;

import ch.ergon.adam.core.db.schema.Schema;
import ch.ergon.adam.core.db.schema.Table;
import ch.ergon.adam.jooq.JooqSource;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.SQLDialect;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class SqliteSource extends JooqSource {


    public SqliteSource(String url, SQLDialect sqlDialect) throws SQLException {
        super(url);
        setSqlDialect(sqlDialect);
    }

    public SqliteSource(Connection connection, SQLDialect sqlDialect) {
        super(connection);
        setSqlDialect(sqlDialect);
    }

    @Override
    public Schema getSchema() {
        Schema schema = super.getSchema();
        setSequences(schema);
        return schema;
    }

    @Override
    protected String getViewDefinition(String name) {
        Result<Record> result = getContext().resultQuery("select sql from sqlite_master where type = 'view' and name = ?", name).fetch();
        String viewDefinition = result.getFirst().getValue("sql").toString();
        viewDefinition = viewDefinition.replaceAll("^(?i)create view [^ ]+ as ", "");
        return viewDefinition;
    }

    @Override
    protected Map<String, List<String>> fetchViewDependencies() {
        return Map.of();
    }

    private void setSequences(Schema schema) {
        Result<Record> result = getContext().resultQuery("SELECT tbl_name FROM sqlite_master WHERE sql LIKE \"%AUTOINCREMENT%\"").fetch();
        result.forEach(record -> {
            String tableName = record.getValue("tbl_name", String.class);
            Table table = schema.getTable(tableName);
            table.getIndexes().forEach(index -> {
                if (index.isPrimary()) {
                    index.getFields().forEach(field -> field.setSequence(true));
                }
            });
        });
    }
}
