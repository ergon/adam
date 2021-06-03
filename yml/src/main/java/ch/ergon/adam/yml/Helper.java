package ch.ergon.adam.yml;

import ch.ergon.adam.core.db.schema.Table;
import ch.ergon.adam.core.db.schema.View;

class Helper {

    public static final String YML = ".yml";

    public static final String TABLE = ".table";

    public static final String VIEW = ".view";

    public static final String ENUMS = "enums.yml";

    public static final String SEQUENCES = "sequences.yml";

    public static String getTableFileName(Table table) {
        return table.getName() + TABLE + YML;
    }

    public static String getViewFileName(View view) {
        return view.getName() + VIEW + YML;
    }

    public static String getEnumFileName() {
        return ENUMS;
    }

    public static String getSequenceFileName() {
        return SEQUENCES;
    }

}
