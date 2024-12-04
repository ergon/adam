package ch.ergon.adam.gradleplugin.util;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.jooq.meta.AbstractDatabase;
import org.jooq.meta.ArrayDefinition;
import org.jooq.meta.CatalogDefinition;
import org.jooq.meta.DefaultEnumDefinition;
import org.jooq.meta.DefaultRelations;
import org.jooq.meta.DomainDefinition;
import org.jooq.meta.EnumDefinition;
import org.jooq.meta.PackageDefinition;
import org.jooq.meta.RoutineDefinition;
import org.jooq.meta.SchemaDefinition;
import org.jooq.meta.SequenceDefinition;
import org.jooq.meta.TableDefinition;
import org.jooq.meta.UDTDefinition;
import org.jooq.meta.XMLSchemaCollectionDefinition;

import ch.ergon.adam.core.db.SourceAndSinkFactory;
import ch.ergon.adam.core.db.schema.Field;
import ch.ergon.adam.core.db.schema.ForeignKey;
import ch.ergon.adam.core.db.schema.Index;
import ch.ergon.adam.core.db.schema.Schema;
import ch.ergon.adam.core.db.schema.Table;

public class AdamDatabase extends AbstractDatabase {
    public static final String SOURCE_PROPERTY = "source";
    private Schema schema;
    private SchemaDefinition schemaDefinition;

    @Override
    protected DSLContext create0() {
        return DSL.using(getConnection());
    }

    @Override
    protected void loadPrimaryKeys(DefaultRelations r) throws SQLException {
        handleIndexes(i -> i.isPrimary(), i -> {
            TableDefinition td = getTable(i.getTable());
            String name = i.getName();
            for (Field field : i.getFields()) {
                r.addPrimaryKey(name, td, td.getColumn(field.getName()));
            }
        });
    }

    @Override
    protected void loadUniqueKeys(DefaultRelations r) throws SQLException {
        handleIndexes(i -> i.isUnique() && !i.isPrimary(), i -> {
            TableDefinition td = getTable(i.getTable());
            String name = i.getName();
            for (Field field : i.getFields()) {
                r.addUniqueKey(name, td, td.getColumn(field.getName()));
            }
        });
    }

    private void handleIndexes(Predicate<Index> filter, Consumer<Index> consumer) {
        for (Table table : schema.getTables()) {
            TableDefinition td = getTable(table);
            if (td == null) {
                // table is excluded from build
                continue;
            }
            table.getIndexes().stream().filter(filter).forEach(consumer);
        }

    }

    @Override
    protected void loadForeignKeys(DefaultRelations r) throws SQLException {
        for (Table table : schema.getTables()) {
            TableDefinition td = getTable(table);
            if (td == null) {
                // table is excluded from build
                continue;
            }
            for (ForeignKey fkey : table.getForeignKeys()) {
                TableDefinition target = getTable(fkey.getTargetIndex().getTable());
                if (target != null) {
                    r.addForeignKey(fkey.getName(), td, td.getColumn(fkey.getField().getName()),
                        fkey.getTargetIndex().getName(), target);
                }
            }
        }
    }

    @Override
    protected void loadCheckConstraints(DefaultRelations r) throws SQLException {
        // not supported
    }

    @Override
    protected List<CatalogDefinition> getCatalogs0() throws SQLException {
        ensureSchema();
        return mutableList(new CatalogDefinition(this, "", ""));
    }

    @Override
    protected List<SchemaDefinition> getSchemata0() throws SQLException {
        ensureSchema();
        return mutableList(new SchemaDefinition(this, "", null));
    }

    @Override
    protected List<SequenceDefinition> getSequences0() throws SQLException {
        // not supported
        return List.of();
    }

    @Override
    protected List<TableDefinition> getTables0() throws SQLException {
        return schema.getTables().stream().map(t -> (TableDefinition) new AdamTableDefinition(schemaDefinition, t))
            .toList();
    }

    @Override
    protected List<RoutineDefinition> getRoutines0() throws SQLException {
        // not supported
        return List.of();
    }

    @Override
    protected List<PackageDefinition> getPackages0() throws SQLException {
        // not supported
        return List.of();
    }

    @Override
    protected List<EnumDefinition> getEnums0() throws SQLException {
        return schema.getEnums().stream().map(e -> {
            DefaultEnumDefinition definition = new DefaultEnumDefinition(schemaDefinition, e.getName(), null);
            definition.addLiterals(e.getValues());
            return (EnumDefinition) definition;
        }).toList();
    }

    @Override
    protected List<DomainDefinition> getDomains0() throws SQLException {
        // not supported
        return List.of();
    }

    @Override
    protected List<XMLSchemaCollectionDefinition> getXMLSchemaCollections0() throws SQLException {
        // not supported
        return List.of();
    }

    @Override
    protected List<UDTDefinition> getUDTs0() throws SQLException {
        // not supported
        return List.of();
    }

    @Override
    protected List<ArrayDefinition> getArrays0() throws SQLException {
        // not supported
        return List.of();
    }

    private TableDefinition getTable(Table table) {
        return getTable(schemaDefinition, table.getName());
    }

    private <T> List<T> mutableList(T value) {
        List<T> list = new ArrayList<>();
        list.add(value);
        return list;
    }

    private void ensureSchema() {
        if (schema == null) {
            String source = (String) getProperties().get(SOURCE_PROPERTY);
            schema = SourceAndSinkFactory.getInstance().getSource(source).getSchema();
            schemaDefinition = new SchemaDefinition(this, "", null);
        }
    }
}
