package ch.ergon.adam.oracle;

import ch.ergon.adam.core.db.schema.*;
import ch.ergon.adam.jooq.JooqSource;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.SQLDialect;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static java.util.Comparator.comparing;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.*;

public class OracleSqlSource extends JooqSource {


    private final String schemaName;
    private Map<String, DbEnum> enums;

    public OracleSqlSource(String url, String schemaName) throws SQLException {
        super(url, schemaName);
        this.schemaName = schemaName;
        this.setSqlDialect(SQLDialect.ORACLE18C);
    }

    public OracleSqlSource(Connection connection, String schemaName) {
        super(connection, schemaName);
        this.setSqlDialect(SQLDialect.ORACLE18C);
        this.schemaName = schemaName;
    }

    @Override
    public Schema getSchema() {
        Schema schema = super.getSchema();
        fetchConstraints(schema);
        return schema;
    }

    @Override
    protected String getViewDefinition(String name) {
        Result<Record> result = getContext().resultQuery("select TEXT from SYS.USER_VIEWS where VIEW_NAME = ?", name).fetch();
        return result.getFirst().getValue("TEXT").toString();
    }

    @Override
    protected Map<String, List<String>> fetchViewDependencies() {
        Result<Record> result = getContext().resultQuery(
            """
                SELECT NAME AS owner, REFERENCED_NAME AS base
                FROM SYS.ALL_DEPENDENCIES
                WHERE TYPE = 'VIEW' AND REFERENCED_TYPE  IN ('VIEW', 'TABLE')
                AND OWNER = ?
                AND REFERENCED_OWNER = OWNER
              """, schemaName).fetch();

        return result.stream()
            .collect(
                groupingBy(r -> r.getValue("OWNER").toString(),
                    mapping(r -> r.getValue("BASE").toString(), toList())));
    }

    private void fetchConstraints(Schema schema) {
        Result<Record> result = getContext().resultQuery(
            """
                SELECT CONSTRAINT_NAME, CONSTRAINT_TYPE, TABLE_NAME, SEARCH_CONDITION FROM ALL_CONSTRAINTS WHERE OWNER = ?
                AND SEARCH_CONDITION_VC not like ('"%" IS NOT NULL')
                """, schemaName).fetch();

        Map<Table, List<Record>> byTable = result.stream()
            .filter(r -> schema.getTable(r.getValue("TABLE_NAME").toString()) != null)
            .collect(groupingBy(
            r -> schema.getTable(r.getValue("TABLE_NAME").toString()),
            toList()));

        byTable.keySet().forEach(table ->
            table.setConstraints(byTable.get(table).stream()
                .map(this::mapConstraintFromOracle)
                .collect(toList())));
    }

    private Constraint mapConstraintFromOracle(Record record) {
        String constraintType = record.getValue("CONSTRAINT_TYPE").toString();
        String name = record.getValue("CONSTRAINT_NAME").toString();
        if (isGeneratedName(name)) {
            name = null;
        }
        switch (constraintType) {
            case "P":
                return new PrimaryKeyConstraint(name);
            case "C":
                RuleConstraint ruleConstraint = new RuleConstraint(name);
                String expression = record.getValue("SEARCH_CONDITION").toString();
                ruleConstraint.setRule(expression);
                return ruleConstraint;
            default:
                throw new RuntimeException(format("Unsupported constraint type [%s]", constraintType));
        }
    }

    @Override
    protected boolean isGeneratedName(String name) {
        return name.startsWith("SYS_");
    }

    @Override
    protected Field mapFieldFromJooq(org.jooq.Field<?> jooqField, org.jooq.Table<?> jooqTable) {
        Field field = super.mapFieldFromJooq(jooqField, jooqTable);
        if (field.getDataType() == DataType.DECIMAL_INTEGER && field.getPrecision() == 19) {
            field.setPrecision(null);
            field.setDataType(DataType.BIGINT);
        }
        return field;
    }
}
