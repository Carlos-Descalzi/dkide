package io.datakitchen.ide.editors.diff.json;

public class AbstractJsonNode implements JsonNode{

    private JsonNode parent;

    public AbstractJsonNode(JsonNode parent){
        this.parent = parent;
    }

    public JsonNode getParent(){
        return parent;
    }
}
