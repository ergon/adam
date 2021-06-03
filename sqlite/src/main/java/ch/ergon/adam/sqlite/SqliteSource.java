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
        schema.setViews(getViews());
        schema.getViews().forEach(view -> view.setFields(schema.getTable(view.getName()).getFields()));
        schema.getTables().removeIf(table -> schema.getView(table.getName()) != null);
        setPrimaryKeys(schema);
        setSequences(schema);
        return schema;
    }

    private Collection<View> getViews() {
        Result<Record> result = getContext().resultQuery("select name, sql from sqlite_master where type = 'view'").fetch();
        return result.stream().map(this::mapViewFromJooq).sorted(comparing(View::getName)).collect(toList());
    }

    private View mapViewFromJooq(Record record) {
        String viewName = record.getValue("name").toString();
        String viewDefinition = record.getValue("sql").toString();
        viewDefinition = viewDefinition.replaceAll("^(?i)create view [^ ]+ as ", "");
        View view = new View(viewName);
        view.setViewDefinition(viewDefinition);
        return view;
    }

    private void setPrimaryKeys(Schema schema) {
        for (Table table : schema.getTables()) {
            Result<Record> result = getContext().resultQuery("PRAGMA table_info('test_table')").fetch();
            for (Record record : result) {
                if (record.getValue("pk", Integer.class) == 0) {
                    continue;
                }
                String fieldName = record.getValue("name", String.class);
                Index primaryKeyIndex = new Index("pk");
                primaryKeyIndex.setPrimary(true);
                primaryKeyIndex.setUnique(true);
                primaryKeyIndex.setFields(newArrayList(table.getField(fieldName)));
                List<Index> newIndexList = Lists.newArrayList(primaryKeyIndex);
                newIndexList.addAll(table.getIndexes());
                table.setIndexes(newIndexList);
                break;
            }
        }
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
