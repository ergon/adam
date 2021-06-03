package ch.ergon.adam.core.helper;

import ch.ergon.adam.core.db.schema.SchemaItem;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;

public class CollectorsHelper
{
    public static <T, K, U> Collector<T, ?, Map<K,U>> toLinkedMap(
        Function<? super T, ? extends K> keyMapper,
        Function<? super T, ? extends U> valueMapper)
    {
        return Collectors.toMap(
            keyMapper,
            valueMapper,
            (u, v) -> {
                throw new IllegalStateException(String.format("Duplicate key %s", u));
            },
            LinkedHashMap::new
        );
    }

    public static <T extends SchemaItem> String createSchemaItemNameList(Collection<T> items, String delimiter) {
        return items.stream().map(SchemaItem::getName).collect(joining(delimiter));
    }

    public static <T extends SchemaItem> String createSchemaItemNameList(Collection<T> items) {
        return createSchemaItemNameList(items, ",");
    }

    public static String createQuotedList(String[] items, String quoteChar) {
        return createQuotedList(items, quoteChar, ",");
    }

    public static String createQuotedList(String[] items, String quoteChar, String delimiter) {
        return stream(items).map(name -> quoteChar + name + quoteChar).collect(joining(delimiter));
    }

    public static <T extends SchemaItem> String[] createSchemaItemNameArray(Collection<T> items) {
        return items.stream().map(SchemaItem::getName).toArray(String[]::new);
    }

}
