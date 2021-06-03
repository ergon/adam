package ch.ergon.adam.sqlite;

import ch.ergon.adam.jooq.JooqSink;
import ch.ergon.adam.core.db.schema.Index;
import org.jooq.CreateIndexStep;
import org.jooq.Name;

import java.sql.Connection;
import java.util.Collection;

import static java.util.stream.Collectors.toList;
import static org.jooq.SQLDialect.SQLITE;

public class SqliteSink extends JooqSink {

    public SqliteSink(String url) {
        super(url);
    }

    public SqliteSink(Connection connection) {
        super(connection, SQLITE);
    }


    @Override
    public void createIndex(Index index) {
        if (index.isPrimary()) {
            return;
        }
        if (index.getName().toLowerCase().startsWith("sqlite_")) {
            Collection<Name> fieldNames = index.getFields().stream().map(this::getFieldName).collect(toList());
            CreateIndexStep createIndex;
            if (index.isUnique()) {
                createIndex = context.createUniqueIndex();
            } else {
                createIndex = context.createIndex();
            }
            createIndex.on(getTableName(index.getTable()), fieldNames).execute();
        } else {
            super.createIndex(index);
        }
    }

    @Override
    public boolean supportAlterAndDropField() {
        return false;
    }
}
