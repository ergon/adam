package ch.ergon.adam.gradleplugin.util;

import org.jooq.codegen.DefaultGeneratorStrategy;
import org.jooq.meta.Definition;
import org.jooq.meta.TableDefinition;

/**
 * Alters jOOQ's DefaultGeneratorStrategy to name tables with an explicit suffix,
 * e.g. {@code PERSON} becomes {@code PersonTable}.
 */
public class TableSuffixGeneratorStrategy extends DefaultGeneratorStrategy {

	@Override
	public String getJavaClassName(Definition definition, org.jooq.codegen.GeneratorStrategy.Mode mode) {
		String defaultName = super.getJavaClassName(definition, mode);
		if (mode == org.jooq.codegen.GeneratorStrategy.Mode.DEFAULT && definition instanceof TableDefinition) {
			return defaultName + "Table";
		} else {
			return defaultName;
		}
	}

}
