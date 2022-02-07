package io.datakitchen.ide.editors.diff.json;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class PatchValue extends AbstractJsonNode implements Patch{

    private Object value;
    private String operation;
    private Object newValue;
    private Action action;
    private boolean done;

    public String toString(){
        return "PatchValue("+value+","+newValue+")";
    }

    public PatchValue(JsonNode parent, Object value, String operation, Object newValue) {
        super(parent);
        this.value = value;
        this.operation = operation;
        this.newValue = newValue;
    }

    public boolean equals(Object other){
        return other instanceof PatchValue
            && new EqualsBuilder()
            .append(value,((PatchValue)other).value)
            .append(operation,((PatchValue)other).operation)
            .append(newValue,((PatchValue)other).newValue)
            .isEquals();
    }

    public int hashCode(){
        return new HashCodeBuilder()
            .append(value)
            .append(operation)
            .append(newValue)
            .toHashCode();
    }

    public Object getValue() {
        return value;
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

    public void apply() {
        System.out.println("Apply value");
        action = Action.APPLY;
        if (operation.equals(OP_REPLACE)){
            System.out.println("Apply replace old"+value+",new:"+newValue);
            value = newValue;
            newValue = null;
        } else if (operation.equals(OP_ADD)){
            System.out.println("Apply add new:"+newValue);
            value = newValue;
            newValue = null;
        } else {
            value = "<<removed>>";
        }
        done = true;
    }

    public void ignore() {
        action = Action.IGNORE;
        done = true;
        newValue = null;
    }

    public boolean isDone(){
        return done;
    }
}
