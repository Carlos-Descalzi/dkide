package io.datakitchen.ide.editors.diff.json;

public class Value extends AbstractJsonNode{

    private Object value;

    public Value(JsonNode parent, Object value) {
        super(parent);
        this.value = value;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public String toString(){
        return String.valueOf(value);
    }
}
