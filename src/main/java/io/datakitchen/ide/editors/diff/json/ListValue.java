package io.datakitchen.ide.editors.diff.json;

import java.util.ArrayList;

public class ListValue extends ArrayList<JsonNode> implements JsonNode {

    JsonNode parent;

    public ListValue(JsonNode parent){
        this.parent = parent;
    }

    @Override
    public JsonNode getParent() {
        return parent;
    }
}
