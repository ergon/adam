package ch.ergon.adam.yml;

import ch.ergon.adam.core.db.interfaces.SchemaSink;
import ch.ergon.adam.core.db.schema.*;
import ch.ergon.adam.yml.schema.*;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static ch.ergon.adam.core.helper.CollectorsHelper.createSchemaItemNameArray;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_DEFAULT;
import static java.util.stream.Collectors.toList;

public class YmlSink implements SchemaSink {

    private final File targetPath;
    private final OutputStream outputStream;
    private final ObjectMapper mapper;
    private final Set<Table> updatedTables = new HashSet<>();
    private final Set<Table> droppedTables = new HashSet<>();
    private final Set<View> updatedViews = new HashSet<>();
    private final Set<View> droppedViews = new HashSet<>();
    private Schema targetSchema;

    public YmlSink(File targetPath) {
        this.targetPath = targetPath;
        this.outputStream = null;
        mapper = YAMLMapper.builder()
            .enable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY)
            .serializationInclusion(NON_DEFAULT)
            .build();
    }

    public YmlSink(OutputStream outputStream) {
        this.targetPath = null;
        this.outputStream = outputStream;
        mapper = YAMLMapper.builder()
            .enable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY)
            .serializationInclusion(NON_DEFAULT)
            .build();
    }

    @Override
    public void setTargetSchema(Schema targetSchema){
        this.targetSchema = targetSchema;
    }

    @Override
    public void commitChanges() {
        try {
            if (targetPath != null && !targetPath.exists()) {
                targetPath.mkdirs();
            }
            for (Table table : updatedTables) {
                writeTableToFile(table);
            }
            for (View view : updatedViews) {
                writeViewToFile(view);
            }
            writeEnumsToFile(this.targetSchema.getEnums());
            writeSequencesToFile(this.targetSchema.getSequences());
            if (outputStream != null) {
                outputStream.close();
            }
        } catch (IOException e) {
            String target;
            if (targetPath != null) {
                target = targetPath.getAbsolutePath();
            } else {
                target = "outputstream";
            }
            throw new RuntimeException("Failed to write schema to [" + target + "].", e);
        }
    }

    @Override
    public void rollback() {

    }

    @Override
    public void dropForeignKey(ForeignKey foreignKey) {
        updatedTables.add(mapToTargetTable(foreignKey.getTable()));
    }

    @Override
    public void createForeignKey(ForeignKey foreignKey) {
        updatedTables.add(foreignKey.getTable());
    }

    @Override
    public void dropIndex(Index index) {
        updatedTables.add(mapToTargetTable(index.getTable()));
    }

    @Override
    public void createIndex(Index index) {
        updatedTables.add(index.getTable());
    }

    @Override
    public void addField(Field field) {
        updatedTables.add(field.getTable());
    }

    @Override
    public void dropField(Field field, Table table) {
        updatedTables.add(mapToTargetTable(table));
    }

    @Override
    public void setDefault(Field field) {
        updatedTables.add(field.getTable());
    }

    @Override
    public void dropDefault(Field field) {
        updatedTables.add(field.getTable());
    }

    @Override
    public void createTable(Table table) {
        updatedTables.add(table);
    }

    @Override
    public void dropTable(Table table) {
        droppedTables.add(table);
    }

    @Override
    public void renameTable(Table oldTable, String newTableName) {
        if (targetSchema.getTable(newTableName) != null) {
            updatedTables.add(targetSchema.getTable(newTableName));
        }
        droppedTables.add(oldTable);
    }

    @Override
    public void copyData(Table sourceTable, Table targetTable, String sourceTableName) {
    }

    @Override
    public void createView(View view) {
        updatedViews.add(view);
    }

    @Override
    public void dropView(View view) {
        droppedViews.add(view);
    }

    @Override
    public void dropEnum(DbEnum dbEnum) {
        // Types are always written
    }

    @Override
    public void createEnum(DbEnum dbEnum) {
        // Types are always written
    }

    @Override
    public void changeFieldType(Field oldField, Field newField, DataType targetDataType) {
        updatedTables.add(newField.getTable());
    }

    @Override
    public void dropConstraint(Constraint constraint) {
        updatedTables.add(mapToTargetTable(constraint.getTable()));
    }

    @Override
    public void createConstraint(Constraint constraint) { updatedTables.add(constraint.getTable()); }

    @Override
    public void dropSequence(Sequence sequence) {
        // Sequences are always written
    }

    @Override
    public void createSequence(Sequence sequence) {
        // Sequences are always written
    }

    @Override
    public void dropSequencesAndDefaults(Table table) {
        // Noop
    }

    @Override
    public void adjustSequences(Table table) {
        // Noop
    }

    private void writeTableToFile(Table table) throws IOException {
        YmlTable ymlTable = mapToYml(table);
        writeToFileOrStream(ymlTable, Helper.getTableFileName(table));
    }

    private void writeViewToFile(View view) throws IOException {
        YmlView ymlView = new YmlView(view.getName());
        ymlView.setViewDefinition(view.getViewDefinition());
        ymlView.setDependencies(createSchemaItemNameArray(view.getBaseRelations()));
        writeToFileOrStream(ymlView, Helper.getViewFileName(view));
    }

    private void writeSequencesToFile(Collection<Sequence> sequences) throws IOException {
        List<YmlSequence> ymlSequences = sequences.stream().map(this::mapToYml).collect(toList());
        writeToFileOrStream(ymlSequences, Helper.getSequenceFileName());
    }

    private void writeEnumsToFile(Collection<DbEnum> enums) throws IOException {
        List<YmlEnum> ymlEnums = enums.stream().map(this::mapToYml).collect(toList());
        writeToFileOrStream(ymlEnums, Helper.getEnumFileName());
    }

    private void writeToFileOrStream(Object object, String fileName) throws IOException {
        if (targetPath != null) {
            File ymlFile = new File(targetPath, fileName);
            mapper.writeValue(ymlFile, object);
        } else {
            mapper.writeValue(outputStream, object);
        }
    }

    private YmlSequence mapToYml(Sequence sequence) {
        YmlSequence ymlSequence = new YmlSequence(sequence.getName());
        ymlSequence.setIncrement(sequence.getIncrement());
        ymlSequence.setMaxValue(sequence.getMaxValue());
        ymlSequence.setMinValue(sequence.getMinValue());
        ymlSequence.setStartValue(sequence.getStartValue());
        return ymlSequence;
    }

    private YmlEnum mapToYml(DbEnum dbEnum) {
        YmlEnum ymlEnum = new YmlEnum(dbEnum.getName());
        ymlEnum.setValues(dbEnum.getValues());
        return ymlEnum;
    }

    private YmlTable mapToYml(Table table) {
        YmlTable ymlTable = new YmlTable(table.getName());
        ymlTable.setFields(table.getFields().stream().map(this::mapToYml).collect(toList()));
        ymlTable.setIndexes(table.getIndexes().stream().map(this::mapToYml).collect(toList()));
        ymlTable.setForeignKeys(table.getForeignKeys().stream().map(this::mapToYml).collect(toList()));
        ymlTable.setRuleConstraints(table.getConstraints().stream()
                .filter(c -> c instanceof RuleConstraint)
                .map(c -> (RuleConstraint)c)
                .map(this::mapToYml).collect(toList()));
        ymlTable.setPreviousName(table.getPreviousName());
        return ymlTable;
    }

    private YmlField mapToYml(Field field) {
        YmlField ymlField = new YmlField(field.getName());
        ymlField.setDataType(field.getDataType().name());
        if (field.getDataType() == DataType.ENUM) {
            ymlField.setEnumName(field.getDbEnum().getName());
        }
        ymlField.setNullable(field.isNullable());
        ymlField.setArray(field.isArray());
        ymlField.setDefaultValue(field.getDefaultValue());
        ymlField.setPrecision(field.getPrecision());
        ymlField.setScale(field.getScale());
        ymlField.setLength(field.getLength());
        ymlField.setSequence(field.isSequence());
        ymlField.setSqlForNew(field.getSqlForNew());
        return ymlField;
    }

    private YmlRuleConstraint mapToYml(RuleConstraint constraint) {
        YmlRuleConstraint ymlConstraint = new YmlRuleConstraint(constraint.getName());
        ymlConstraint.setRule(constraint.getRule());
        return ymlConstraint;
    }

    private YmlIndex mapToYml(Index index) {
        YmlIndex ymlIndex = new YmlIndex(index.getName());
        ymlIndex.setPrimary(index.isPrimary());
        ymlIndex.setUnique(index.isUnique());
        ymlIndex.setWhere(index.getWhere());
        ymlIndex.setFields(createSchemaItemNameArray(index.getFields()));
        return ymlIndex;
    }

    private YmlForeignKey mapToYml(ForeignKey foreignKey) {
        YmlForeignKey ymlForeignKey = new YmlForeignKey(foreignKey.getName());
        ymlForeignKey.setField(foreignKey.getField().getName());
        ymlForeignKey.setTargetIndex(foreignKey.getTargetIndex().getName());
        ymlForeignKey.setTargetTable(foreignKey.getTargetIndex().getTable().getName());
        return ymlForeignKey;

    }

    private Table mapToTargetTable(Table table) {
        return targetSchema.getTable(table.getName());
    }

    @Override
    public void close() {

    }
}
