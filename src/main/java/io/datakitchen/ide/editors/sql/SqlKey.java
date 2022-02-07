package io.datakitchen.ide.editors.sql;

import io.datakitchen.ide.ui.NamedObject;
import org.apache.commons.lang3.StringUtils;

import java.util.LinkedHashMap;
import java.util.Map;

public class SqlKey implements NamedObject {
    private String name;
    private Map<String, Object> content = new LinkedHashMap<>();

    public SqlKey(){}

    public SqlKey(String name, Map<String, Object> value) {
        this.name = name;
        this.content = new LinkedHashMap<>(value);
    }

    public SqlKey(String name) {
        this(name, new LinkedHashMap<>());
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, Object> getContent() {
        return content;
    }

    public void setContent(Map<String, Object> content) {
        this.content = content;
    }

    public static SqlKey fromEntry(Map.Entry<String, Object> entry) {
        return new SqlKey(entry.getKey(), (Map<String, Object>) entry.getValue());
    }

    public String toString(){
        return StringUtils.isBlank(name) ? "(no name)" : name;
    }
}
