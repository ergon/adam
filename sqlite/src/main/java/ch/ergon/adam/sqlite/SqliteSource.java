package ch.ergon.adam.sqlite;

import ch.ergon.adam.jooq.JooqSource;
import ch.ergon.adam.core.db.schema.Index;
import ch.ergon.adam.core.db.schema.Schema;
import ch.ergon.adam.core.db.schema.Table;
import ch.ergon.adam.core.db.schema.View;
import com.google.common.collect.Lists;
import org.jooq.Record;
import org.jooq.Result;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;
import static org.jooq.SQLDialect.SQLITE;

public class SqliteSource extends JooqSource {


    public SqliteSource(String url) throws SQLException {
        super(url);
        setSqlDialect(SQLITE);
    }

    public SqliteSource(Connection connection) {
        super(connection);
        setSqlDialect(SQLITE);
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
