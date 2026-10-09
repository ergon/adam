package ch.ergon.adam.sqlite;

import ch.ergon.adam.core.db.schema.Index;
import ch.ergon.adam.jooq.JooqSink;
import com.google.common.base.Strings;
import org.jooq.CreateIndexIncludeStep;
import org.jooq.CreateIndexStep;
import org.jooq.Name;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import java.sql.Connection;
import java.util.Collection;

import static java.util.stream.Collectors.toList;

public class SqliteSink extends JooqSink {

    public SqliteSink(String url, SQLDialect dialect) {
        super(url, dialect);
    }

    public SqliteSink(Connection connection, SQLDialect dialect) {
        super(connection, dialect);
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

            CreateIndexIncludeStep indexStep = createIndex.on(getTableName(index.getTable()), fieldNames);
            if (Strings.isNullOrEmpty(index.getWhere())) {
                indexStep.execute();
            } else {
                indexStep.where(DSL.condition(index.getWhere())).execute();
            }
        } else {
            super.createIndex(index);
        }
    }

    @Override
    public boolean supportAlterAndDropField() {
        return false;
    }
}
