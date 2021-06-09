# **A**dvanced **DA**tabase **M**igration (ADAM)

## ADAM

ADAM has been built to support developer- and devops-teams to keep schemas of different databases (development, testing, production) in sync to the required
software version. The schema is described in files kept and managed next to the application code. This allows to work on multiple branches and merging changes
to the schema in the same way as it is already done for the source code. When switching/releasing to a different version, ADAM compares the actual database
schema with the expected one and applies the required changes. Since not all cases can be supported by ADAM directly, it allows adding manual migration script
which may be executed before or after the automated schema migration.

## Overview

ADAM consist of the following components:

### Schema source

A schema source provides a schema. Normally, this will be a set of files stored together with the code but can also be a schema of an actual database.

Supported:

- YML-Files
- PostgreSQL
- SQLite

### Schema sink

The schema sink is used to apply the required schema changes.

Supported:

- YML-Files
- PostgreSQL
- SQLite

### Automated schema migrator

The automated schema migrator extracts the difference of two schema sources and applies the difference to a schema sink. Normally, the first schema source is
also the schema sink, and the second (target) schema source is the one stored in the code repository. The task of this component is to make sure that the schema
of a database is the same as the reference schema. During this migration, the data in the database will be preserved.

Depending on the involved schema types, the following database objects are supported:

- Tables (fields, field types, default, nullability, field size)
- Views
- Indexes (primary, unique, non-unique)
- Foreign keys

ADAM will automatically try to find the correct order in which the objects need to be dropped, altered or (re)created.

### Manual script executor

If a use case is not supported by the automated schema migrator, it is possible to write specific migration scripts. These scripts are as well stored together
with schema description next to the code.

The type of script is described by two characteristics:

- When will it be executed?
  - Pre-migration: prior the automated schema migration
  - Post-migration: after the automated schema migration

- How often will it be executed?
  - Once: the script executor will make sure that the script is only executed once
  - Always: the script will always be executed then the migration runs
  - Init: the script will only be executed on an empty database

#### Once

In case of the **Once**-scripts the script executor needs to decide which scripts need to be executed in which order. For this, the scripts are assigned to an
artificial version of the software. On every migration, the current version is stored in a separate table `db_schema_version` on the database itself. To check
which scripts need to be executed, the script executor checks which scripts have been added since the current version of the database. The order of execution is
then given by the artificial version.

---
**NOTE**

The current version of ADAM uses the git hash as this artificial version. It is currently required that the schema and script files are stored in a git
repository.
---

#### Always / Init

For the **Always**- and **Init**-scripts, only to order to execute the scripts needs to determined. There the file name is used as the sort order.

The **Init**-scripts are only executed if the table `db_schema_version` is not yet there.

### Migration process

On every migration, ADAM executes the following steps:

#### If the table `db_schema_version` does not exist (or is empty)

1. Add entry to `db_schema_version`
1. Create the database schema if it does not exist
1. Execute all pre-init scripts
1. Execute the automated schema migration
1. Execute all post-init scripts
1. Mark entry in `db_schema_version` as completed

#### If the table `db_schema_version` contains a previous version

1. Add entry to `db_schema_version`
1. Execute all pre-always scripts
1. Execute added pre-once scripts
1. Execute the automated schema migration
1. Execute added post-once scripts
1. Execute all post-always scripts
1. Mark entry in `db_schema_version` as completed

If a migration fails, ADAM will try to rollback the changes or mark the migration as failed in `db_schema_version`. In such a case, manual intervention will be
required to fix the problem.

## Usage

ADAM can be used as a library in a Java application or executed using the Gradle plugin.

### Gradle Plugin

```groovy
buildscript {
  dependencies {
    classpath 'ch.ergon.adam:postgresql:1.0.0'
    classpath 'ch.ergon.adam:yml:1.0.0'
  }
}

plugins {
  id 'ch.ergon.adam' version '1.0.0'
}

adam {
  targetUrl = 'jdbc:postgresql://localhost:5432/dbname?user=username&password=password&currentSchema=dbschema'

  // configure gradle tasks
  // ...
}
```

#### Gradle Tasks

##### adamMigrateDb

Execute the database migration

```groovy
adam {
  adamMigrateDb {
    // Migrate even if source and target version match
    migrateSameVersion
    // Execute automated migration even if source (DB) version is unknown.
    // Pre- and post-migration scripts will not be executed in this case.
    allowUnknownDBVersion
    // Execute migration even if source (DB) version is not an ancestor of the target version.
    allowNonForwardMigration
  }
}
```

##### adamExportGitHistory

```groovy
adam {
  adamExportGitHistory {
    // Override the git repo path
    gitRepo
    // Path to the history file
    historyExportFile
  }
}
```

##### adamExportMigrationScripts

Export the migration scripts ready for deployment without git repo

```groovy
adam {
  adamExportMigrationScripts {
    // Override the git repo path
    gitRepo
    // Source path of the pre- and post-migration scripts
    migrationScriptSourcePath
    // Export path of the scripts
    migrationScriptExportPath
  }
}
```

#### adamExportTargetVersion

Export the target version (git hash of HEAD)

```groovy
adam {
  adamExportTargetVersion {
    // Override the exported version
    targetVersion
    // Path to target version file
    targetVersionFile
  }
}
```

#### adamCleanDb

Clean (drop the schema) the database defined by `targetUrl`

```
adam {
  adamCleanDb {
    // Target database url
    targetUrl
  }
}
```

### YAML file definition

#### *.table.yml

```yaml
name: demo
fields:
  - name: id
    dataType: BIGINT
    sequence: true
  - name: name
    dataType: VARCHAR
    nullable: true
indexes: []
foreignKeys: []
ruleConstraints: []
```
The JSON Schema definition is in [yml/table-schema.json](yml/table-schema.json) and offers autocompletion for YML files in *IntelliJ*.
JSON Schemas need to be configured for IntelliJ support:
* *Languages & Frameworks* - *Schemas and DTDs* - *JSON Schema Mappings*:
* Name: *ADAM Table Schema*
* Schema file or URL: *path/to/yml/table-schema.json*
* Schema version: JSON Schema version 7
* File path pattern: _*.table.yml_

Other example files: `test.table.yml` in `integration-test-db` module.

#### *.view.yml

```yaml
name: product_view
viewDefinition: >
  SELECT product.*, global_product.base_unit FROM product
  JOIN global_product on product.product_no = global_product.product_no
dependencies: # define dependencies on other tables and views for correct migration
  - product
  - global_product
```

#### enums.yml

```yaml
- name: status
  values:
    - OPEN
    - IN_PROGRESS
    - DONE
```
