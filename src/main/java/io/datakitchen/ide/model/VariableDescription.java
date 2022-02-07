package io.datakitchen.ide.model;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class VariableDescription {

    public static final VariableDescription VAR_ROW_COUNT = new VariableDescription("row_count", "Row count", MetricConversion.INTEGER);
    public static final VariableDescription VAR_COLUMN_COUNT = new VariableDescription("column_count", "Column count", MetricConversion.INTEGER);
    public static final VariableDescription VAR_RESULT = new VariableDescription("result", "Result");
    public static final VariableDescription VAR_SIZE = new VariableDescription("size", "Size", MetricConversion.INTEGER);
    public static final VariableDescription VAR_SHA = new VariableDescription("sha", "SHA", MetricConversion.STRING);
    public static final VariableDescription VAR_MD5 = new VariableDescription("md5", "MD5", MetricConversion.STRING);
    public static final VariableDescription VAR_KEY_NAMES = new VariableDescription("key_names", "Key names");
    public static final VariableDescription VAR_KEY_MAP = new VariableDescription("key_map", "Key map");
    public static final VariableDescription VAR_KEY_FILES = new VariableDescription("key_files", "Key files");
    public static final VariableDescription VAR_KEY_COUNT = new VariableDescription("key_count", "Key count", MetricConversion.INTEGER);

    public static final Set<VariableDescription> SQL_CONN_VARS = Set.of(VAR_KEY_NAMES, VAR_KEY_COUNT, VAR_KEY_MAP);
    public static final Set<VariableDescription> SQL_KEY_VARS = Set.of(VAR_ROW_COUNT, VAR_COLUMN_COUNT, VAR_RESULT);
    public static final Set<VariableDescription> SQL_SINK_KEY_VARS = Set.of(VAR_ROW_COUNT, VAR_COLUMN_COUNT);
    public static final Set<VariableDescription> FILE_CONN_VARS = Set.of(VAR_KEY_NAMES, VAR_KEY_COUNT, VAR_KEY_FILES, VAR_KEY_MAP);
    public static final Set<VariableDescription> FILE_KEY_VARS = Set.of(VAR_SIZE, VAR_SHA, VAR_MD5, VAR_ROW_COUNT);

    public static final Set<VariableDescription> ALL = Set.of(
            VAR_ROW_COUNT,
            VAR_COLUMN_COUNT,
            VAR_RESULT,
            VAR_SIZE,
            VAR_SHA,
            VAR_MD5,
            VAR_KEY_NAMES,
            VAR_KEY_MAP,
            VAR_KEY_FILES,
            VAR_KEY_COUNT
    );

    private static final Map<String, VariableDescription> BY_NAME =
            ALL.stream().collect(Collectors.toMap(VariableDescription::getName, (VariableDescription v)->v));

    private final String name;
    private final String displayName;
    private final MetricConversion type;

    public VariableDescription(String name, String displayName) {
        this(name, displayName, null);
    }

    public VariableDescription(String name, String displayName, MetricConversion type) {
        this.name = name;
        this.displayName = displayName;
        this.type = type;
    }

    public static VariableDescription fromName(String name) {
        VariableDescription v = BY_NAME.get(name);
        if (v == null){
            v = new VariableDescription(name,name);
        }
        return v;
    }

    public String getName() {
        return name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String toString(){
        return displayName;
    }

    public MetricConversion getType() {
        return type;
    }

    public boolean equals(Object other){
        return other instanceof VariableDescription
            && new EqualsBuilder()
                .append(name, ((VariableDescription)other).name)
                .append(displayName, ((VariableDescription)other).displayName)
                .append(type, ((VariableDescription) other).type)
                .isEquals();
    }

    public int hashCode(){
        return new HashCodeBuilder()
                .append(name)
                .append(displayName)
                .append(type)
                .toHashCode();
    }
}
