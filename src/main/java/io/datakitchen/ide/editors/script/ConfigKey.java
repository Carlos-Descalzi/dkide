package io.datakitchen.ide.editors.script;

import io.datakitchen.ide.ui.NamedObject;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class ConfigKey implements NamedObject {
    private String name;
    private Map<String, Object> content;

    private void notifyChanges() {
    }

    public static ConfigKey fromEntry(Map.Entry<String, Object> entry){
        return new ConfigKey(entry.getKey(), (Map<String, Object>)entry.getValue());
    }

    public ConfigKey() {
        this(null, new LinkedHashMap<>());
        content.put("export",new ArrayList<>());
        content.put("parameters",new LinkedHashMap<>());
        content.put("script","");
    }

    public ConfigKey(String name) {
        this(name, new LinkedHashMap<>());
        content.put("export",new ArrayList<>());
        content.put("parameters",new LinkedHashMap<>());
        content.put("script","");
    }

    public ConfigKey(String name, Map<String, Object> content) {
        this.name = name;
        this.content = content;
    }

    public String toString(){
        return StringUtils.isBlank(name) ? "(no name)" : name;
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
}
