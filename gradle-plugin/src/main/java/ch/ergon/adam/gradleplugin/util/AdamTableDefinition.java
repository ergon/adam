package ch.ergon.adam.gradleplugin.util;

import ch.ergon.adam.core.db.schema.Field;
import ch.ergon.adam.core.db.schema.Table;
import org.jooq.Name;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.jooq.meta.*;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AdamTableDefinition extends AbstractTableDefinition {

    private final Table table;

    public AdamTableDefinition(SchemaDefinition schema, Table table) {
        super(schema, table.getName(), null);
        this.table = table;
    }

    @Override
    protected List<ColumnDefinition> getElements0() throws SQLException {
        List<ColumnDefinition> result = new ArrayList<ColumnDefinition>();
        for (Field field : table.getFields()) {
            result.add(new DefaultColumnDefinition(this, field.getName(), field.getIndex(), dataTypeDefinition(field),
                isIdentity(field), null));
        }
        return result;
    }

    private DataTypeDefinition dataTypeDefinition(Field field) {
        return new DefaultDataTypeDefinition(getDatabase(), getSchema(),
            typeName(field), field.getLength(), field.getPrecision(), field.getScale(),
            field.isNullable(), field.getDefaultValue(), getUserType(field));
    }

	private String typeName(Field field) {
		var jooqType = switch (field.getDataType()) {
		case BIGINT -> SQLDataType.BIGINT;
		case BIGINTUNSIGNED -> SQLDataType.BIGINTUNSIGNED;
		case BINARY -> SQLDataType.BINARY;
		case BIT -> SQLDataType.BIT;
		case BLOB -> SQLDataType.BLOB;
		case BOOLEAN -> SQLDataType.BOOLEAN;
		case CHAR -> SQLDataType.CHAR;
		case CLOB -> SQLDataType.CLOB;
		case DATE -> SQLDataType.DATE;
		case DECIMAL -> SQLDataType.DECIMAL;
		case DECIMAL_INTEGER -> SQLDataType.DECIMAL_INTEGER;
		case DOUBLE -> SQLDataType.DOUBLE;
		case ENUM -> SQLDataType.VARCHAR;
		case FLOAT -> SQLDataType.FLOAT;
		case INTEGER -> SQLDataType.INTEGER;
		case INTEGERUNSIGNED -> SQLDataType.INTEGERUNSIGNED;
		case INTERVALDAYTOSECOND -> SQLDataType.INTERVALDAYTOSECOND;
		case INTERVALYEARTOMONTH -> SQLDataType.INTERVALYEARTOMONTH;
		case INTERVALYEARTOSECOND -> SQLDataType.INTERVAL;
		case LOCALDATE -> SQLDataType.LOCALDATE;
		case LOCALDATETIME -> SQLDataType.LOCALDATETIME;
		case LOCALTIME -> SQLDataType.LOCALTIME;
		case LONGNVARCHAR -> SQLDataType.LONGNVARCHAR;
		case LONGVARBINARY -> SQLDataType.LONGVARBINARY;
		case LONGVARCHAR -> SQLDataType.LONGVARCHAR;
		case NCHAR -> SQLDataType.NCHAR;
		case NCLOB -> SQLDataType.NCLOB;
		case NUMERIC -> SQLDataType.NUMERIC;
		case NVARCHAR -> SQLDataType.NVARCHAR;
		case OFFSETDATETIME -> SQLDataType.OFFSETDATETIME;
		case OFFSETTIME -> SQLDataType.OFFSETTIME;
		case REAL -> SQLDataType.REAL;
		case SMALLINT -> SQLDataType.SMALLINT;
		case SMALLINTUNSIGNED -> SQLDataType.SMALLINTUNSIGNED;
		case TIME -> SQLDataType.TIME;
		case TIMESTAMP -> SQLDataType.TIMESTAMP;
		case TIMESTAMPWITHTIMEZONE -> SQLDataType.TIMESTAMPWITHTIMEZONE;
		case TIMEWITHTIMEZONE -> SQLDataType.TIMEWITHTIMEZONE;
		case TINYINT -> SQLDataType.TINYINT;
		case TINYINTUNSIGNED -> SQLDataType.TINYINTUNSIGNED;
		case UUID -> SQLDataType.UUID;
		case VARBINARY -> SQLDataType.VARBINARY;
		case VARCHAR -> SQLDataType.VARCHAR;
		};

        String typeName = jooqType.getTypeName();
        if (field.isArray()) {
            return getArrayTypeName(typeName);
        }
		return typeName;
	}

    /**
     * Keep in sync with default case in {@link org.jooq.meta.AbstractDatabase#isArrayType(String dataType)}.
     */
    private String getArrayTypeName(String typeName) {
        return typeName + " ARRAY";
    }

    private boolean isIdentity(Field field) {
        return field.isSequence();
    }

    private Name getUserType(Field field) {
        if (field.getDbEnum() != null) {
            return DSL.name(field.getDbEnum().getName());
        }
        return null;
    }
}
