package io.datakitchen.ide.editors.diff.json;

public interface Patch extends JsonNode{
    String OP_ADD = "add";
    String OP_REPLACE = "replace";
    String OP_REMOVE = "remove";

    enum Action {
        APPLY,
        IGNORE
    }
    Object getValue();

    String getOperation();

    Object getNewValue();

    Action getAction();

    void apply();

    void ignore();

    boolean isDone();

}
