package io.datakitchen.ide.editors.diff.json;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class PatchKeyValuePair<K,V> extends KeyValuePair implements Patch{

    private String operation;
    private Object newValue;
    private Patch.Action action;
    private boolean done;

    public PatchKeyValuePair(JsonNode parent, Object key, Object value, String operation, Object newValue) {
        super(parent, key, value);
        this.operation = operation;
        this.newValue = newValue;
    }

    public boolean equals(Object other){
        return other instanceof PatchKeyValuePair
                && new EqualsBuilder()
                .append(getKey(), ((PatchKeyValuePair)other).getKey())
                .append(getValue(), ((PatchKeyValuePair)other).getValue())
                .append(operation, ((PatchKeyValuePair)other).operation)
                .append(newValue, ((PatchKeyValuePair)other).newValue)
                .isEquals();
    }

    public int hashCode(){
        return new HashCodeBuilder()
            .append(getKey())
            .append(getValue())
            .append(operation)
            .append(newValue)
            .toHashCode();
    }

    public String getOperation() {
        return operation;
    }

    public Object getNewValue() {
        return newValue;
    }

    public Action getAction() {
        return action;
    }
    @Override
    public void apply() {
        action = Action.APPLY;
        if (operation.equals(OP_REPLACE)){
            setValue(newValue);
            newValue = null;
        } else if (operation.equals(OP_ADD)){
            setValue(newValue);
            newValue = null;
        } else {
            setValue("<<removed>>");
        }
        done = true;
    }

    @Override
    public void ignore() {
        newValue = null;
        done = true;
        action = Action.IGNORE;
    }

    public boolean isDone(){
        return done;
    }
}
