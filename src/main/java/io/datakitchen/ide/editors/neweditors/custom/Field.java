package io.datakitchen.ide.editors.neweditors.custom;

import io.datakitchen.ide.util.ObjectUtil;

import java.util.List;
import java.util.Map;

public class Field {
    public enum Type {
        SINGLE_ENTRY,
        LIST,
        OBJECT
    }
    private final String displayName;
    private final String description;
    private final Type type;
    private final Change[] changes;

    public Field(String displayName, String description, Type type, Change[] changes) {
        this.displayName = displayName;
        this.description = description;
        this.type = type;
        this.changes = changes;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public Change[] getChanges() {
        return changes;
    }

    public Type getType() {
        return type;
    }

    public static Field fromJson(Map<String, Object> json){
        List<Map<String, String>> changes = ObjectUtil.cast(json.get("apply"));
        return new Field(
            (String)json.get("display-name"),
            (String)json.get("description"),
            Type.valueOf((String)json.getOrDefault("type", Type.SINGLE_ENTRY.toString())),
            changes.stream().map(Change::fromJson).toArray(Change[]::new)
        );
    }
}
