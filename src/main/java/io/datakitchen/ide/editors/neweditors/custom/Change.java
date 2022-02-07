package io.datakitchen.ide.editors.neweditors.custom;

import java.util.Map;

public class Change {
    private final String nodeFile;
    private final String jsonPointer;
    private final String operation;

    public Change(String nodeFile, String jsonPointer, String operation) {
        this.nodeFile = nodeFile;
        this.jsonPointer = jsonPointer;
        this.operation = operation;
    }

    public String getNodeFile() {
        return nodeFile;
    }

    public String getJsonPointer() {
        return jsonPointer;
    }

    public String getOperation() {
        return operation;
    }

    public static Change fromJson(Map<String, String> json) {
        return new Change(
                json.get("node-file"),
                json.get("json-pointer"),
                json.getOrDefault("op", "replace"));
    }
}
