package ch.ergon.adam.core;

import java.util.Collection;

import static ch.ergon.adam.core.prepost.db_schema_version.DbSchemaVersionSource.SCHEMA_VERSION_TABLE_NAME;
import static com.google.common.collect.Lists.newArrayList;

public class MigrationConfiguration {

    private Collection<String> objectNameIncludeList;

    private Collection<String> objectNameExcludeList = newArrayList(SCHEMA_VERSION_TABLE_NAME);

    public Collection<String> getObjectNameIncludeList() {
        return objectNameIncludeList;
    }

    public void setObjectNameIncludeList(Collection<String> objectNameIncludeList) {
        this.objectNameIncludeList = objectNameIncludeList;
    }

    public Collection<String> getObjectNameExcludeList() {
        return objectNameExcludeList;
    }

    public void setObjectNameExcludeList(Collection<String> objectNameExcludeList) {
        this.objectNameExcludeList = objectNameExcludeList;
    }
}
