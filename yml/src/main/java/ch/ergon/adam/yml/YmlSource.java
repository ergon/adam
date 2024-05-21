package ch.ergon.adam.yml;

import ch.ergon.adam.core.db.schema.*;
import ch.ergon.adam.yml.schema.*;
import ch.ergon.adam.core.db.interfaces.SchemaSource;
import ch.ergon.adam.core.filetree.FileTreeTraverser;
import ch.ergon.adam.core.helper.Pair;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static ch.ergon.adam.core.db.schema.DataType.ENUM;
import static ch.ergon.adam.core.helper.CollectorsHelper.toLinkedMap;
import static com.google.common.base.Strings.isNullOrEmpty;
import static java.lang.String.format;
import static java.util.Arrays.stream;
import static java.util.Objects.requireNonNull;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toList;

public class YmlSource implements SchemaSource {

    private final static Logger logger = LoggerFactory.getLogger(YmlSequence.class);
    private static final String INDEX_NAME_PREFIX = "idx";
    private static final String RULE_NAME_PREFIX = "rule";
    private static final String FK_NAME_PREFIX = "fk";

    private final FileTreeTraverser fileTraverser;
    private final ObjectMapper mapper;

    public YmlSource(FileTreeTraverser fileTraverser) {
        this.fileTraverser = fileTraverser;
        mapper = new ObjectMapper(new YAMLFactory());
    }

    @Override
    public Schema getSchema() {
        logger.info("Loading YML schema from [" + fileTraverser + "].");
        Schema schema = new Schema();
        schema.setEnums(loadEnums());
        schema.setSequences(loadSequences());

        List<String> fileNames = fileTraverser.getFileNames();

        List<Pair<Table, YmlTable>> tables = fileNames.stream()
            .filter(fileName -> fileName.endsWith(Helper.TABLE + Helper.YML))
            .map(fileName -> loadTable(fileName, schema))
                .collect(toList());

        List<YmlView> ymlViews = fileNames.stream()
            .filter(fileName -> fileName.endsWith(Helper.VIEW + Helper.YML))
                .map(this::loadView)
                .collect(toList());

        Map<String, Table> tablesByName = tables.stream().map(Pair::getFirst).collect(toLinkedMap(Table::getName, identity()));
        tables.stream().map(Pair::getSecond).forEach(ymlTable -> {
            setupForeignKeys(ymlTable, tablesByName);
        });

        Map<String, View> viewsByName = ymlViews.stream().map(this::mapFromYml).collect(toLinkedMap(View::getName, identity()));

        schema.setTables(tablesByName.values());
        schema.setViews(viewsByName.values());


        ymlViews.forEach(ymlView -> {
            setupViewDependencies(ymlView, schema);
        });

        return schema;
    }

    private void setupViewDependencies(YmlView ymlView, Schema schema) {
        View view = schema.getView(ymlView.getName());
        stream(ymlView.getDependencies()).map(schema::getRelation).forEach(view::addBaseRelation);
    }

    private Collection<Sequence> loadSequences() {
        try (InputStream inputStream = fileTraverser.openFile(Helper.getSequenceFileName())) {
            if (inputStream == null) {
                return new LinkedList<>();
            }
            YmlSequence[] ymlSequences = mapper.readValue(inputStream, YmlSequence[].class);
            return stream(ymlSequences).map(this::mapFromYml).collect(toList());
        } catch (IOException e) {
            throw new RuntimeException("Failed to load table from file [" + Helper.getSequenceFileName() + "]", e);
        }
    }

    private Collection<DbEnum> loadEnums() {
        try (InputStream inputStream = fileTraverser.openFile(Helper.getEnumFileName())) {
            if (inputStream == null) {
                return new LinkedList<>();
            }
            YmlEnum[] ymlEnums = mapper.readValue(inputStream, YmlEnum[].class);
            return stream(ymlEnums).map(this::mapFromYml).collect(toList());
        } catch (IOException e) {
            throw new RuntimeException("Failed to load table from file [" + Helper.getEnumFileName() + "]", e);
        }
    }

    private void setupForeignKeys(YmlTable ymlTable, Map<String, Table> tablesByName) {
        Table table = tablesByName.get(ymlTable.getName());
        table.setForeignKeys(ymlTable.getForeignKeys().stream()
            .map(ymlForeignKey -> mapFromYml(ymlForeignKey, table, tablesByName, ymlTable.getForeignKeys().indexOf(ymlForeignKey)))
            .collect(toList()));
    }

    private Pair<Table, YmlTable> loadTable(String fileName, Schema schema) {
        try (InputStream inputStream = fileTraverser.openFile(fileName)) {
            YmlTable ymlTable = mapper.readValue(inputStream, YmlTable.class);
            return new Pair<>(mapFromYml(ymlTable, schema), ymlTable);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load table from file [" + fileName + "]", e);
        }
    }

    private YmlView loadView(String fileName) {
        try (InputStream inputStream = fileTraverser.openFile(fileName)) {
            YmlView ymlView = mapper.readValue(inputStream, YmlView.class);
            return ymlView;
        } catch (IOException e) {
            throw new RuntimeException("Failed to load table from file [" + fileName + "]", e);
        }
    }

    private Sequence mapFromYml(YmlSequence ymlSequence) {
        Sequence sequence = new Sequence(ymlSequence.getName());
        sequence.setIncrement(ymlSequence.getIncrement());
        sequence.setMaxValue(ymlSequence.getMaxValue());
        sequence.setMinValue(ymlSequence.getMinValue());
        sequence.setStartValue(ymlSequence.getStartValue());
        return sequence;
    }

    private DbEnum mapFromYml(YmlEnum ymlEnum) {
        DbEnum dbEnum = new DbEnum(ymlEnum.getName());
        dbEnum.setValues(ymlEnum.getValues());
        return dbEnum;
    }

    private View mapFromYml(YmlView ymlView) {
        View view = new View(ymlView.getName());
        view.setViewDefinition(ymlView.getViewDefinition());
        return view;
    }

    private Table mapFromYml(YmlTable ymlTable, Schema schema) {
        Table table = new Table(ymlTable.getName());
        table.setFields(ymlTable.getFields().stream()
            .map(ymlField -> mapFromYml(ymlField, schema, table))
            .collect(toList()));
        table.setIndexes(ymlTable.getIndexes().stream()
            .map(ymlIndex -> mapFromYml(ymlIndex, table, ymlTable.getIndexes().indexOf(ymlIndex)))
            .collect(toList()));
        table.setConstraints(ymlTable.getRuleConstraints().stream()
            .map(ymlRule -> mapFromYml(ymlRule, ymlTable, ymlTable.getRuleConstraints().indexOf(ymlRule)))
            .collect(toList()));
        table.setPreviousName(ymlTable.getPreviousName());
        return table;
    }

    private Field mapFromYml(YmlField ymlField, Schema schema, Table table) {
        Field field = new Field(ymlField.getName());
        field.setDataType(DataType.valueOf(ymlField.getDataType()));
        if (field.getDataType() == ENUM) {
            field.setDbEnum(requireNonNull(schema.getEnum(ymlField.getEnumName()), format("enum %s not found for field %s.%s", ymlField.getEnumName(), table.getName(), ymlField.getName())));
        }
        field.setNullable(ymlField.isNullable());
        field.setArray(ymlField.isArray());
        field.setDefaultValue(ymlField.getDefaultValue());
        field.setLength(ymlField.getLength());
        field.setPrecision(ymlField.getPrecision());
        field.setScale(ymlField.getScale());
        field.setSequence(ymlField.isSequence());
        field.setSqlForNew(ymlField.getSqlForNew());
        return field;
    }

    private Index mapFromYml(YmlIndex ymlIndex, Table table, int position) {
        String indexName = ymlIndex.getName();
        if (isNullOrEmpty(indexName)) {
            indexName = INDEX_NAME_PREFIX + "_" + table.getName() + "_" + position;
        }
        Index index = new Index(indexName);
        index.setPrimary(ymlIndex.isPrimary());
        index.setUnique(ymlIndex.isPrimary() ? true : ymlIndex.isUnique());
        index.setWhere(ymlIndex.getWhere());
        index.setFields(stream(ymlIndex.getFields())
            .map(name -> requireNonNull(table.getField(name), format("field %s.%s not found for index %s", table.getName(), name, ymlIndex.getName())))
            .collect(toList()));
        return index;
    }

    private RuleConstraint mapFromYml(YmlRuleConstraint ymlRuleConstraint, YmlTable table, int position) {
        String name = ymlRuleConstraint.getName();
        if (isNullOrEmpty(name)) {
            name = RULE_NAME_PREFIX + "_" + table.getName() + "_" + position;
        }
        RuleConstraint ruleConstraint = new RuleConstraint(name);
        ruleConstraint.setRule(ymlRuleConstraint.getRule());
        return ruleConstraint;
    }

    private ForeignKey mapFromYml(YmlForeignKey ymlForeignKey, Table table, Map<String, Table> tablesByName, int position) {
        String name = ymlForeignKey.getName();
        if (isNullOrEmpty(name)) {
            name = FK_NAME_PREFIX + "_" + table.getName() + "_" + position;
        }
        ForeignKey foreignKey = new ForeignKey(name);
        String foreignKeyName = table.getName() + "." + foreignKey.getName();
        foreignKey.setField(requireNonNull(table.getField(ymlForeignKey.getField()), format("field %s not found for foreign key %s", ymlForeignKey.getField(), foreignKeyName)));
        Table targetTable = requireNonNull(tablesByName.get(ymlForeignKey.getTargetTable()), format("table %s not found for foreign key %s", ymlForeignKey.getTargetTable(), foreignKeyName));
        if (isNullOrEmpty(ymlForeignKey.getTargetIndex())) {
            Index foreignPrimaryKey = targetTable.getIndexes().stream().filter(Index::isPrimary).findAny()
                .orElseThrow(() -> new NullPointerException(format("targetIndex for foreign key on table %s not set and no primary key found on target table %s", table.getName(), targetTable.getName())));
            foreignKey.setTargetIndex(foreignPrimaryKey);
        } else {
            foreignKey.setTargetIndex(requireNonNull(targetTable.getIndex(ymlForeignKey.getTargetIndex()), format("index %s.%s not found for foreign key %s", ymlForeignKey.getTargetTable(), ymlForeignKey.getTargetIndex(), foreignKeyName)));
        }
        return foreignKey;
    }

    @Override
    public void close() {

    }
}
