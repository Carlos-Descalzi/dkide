package io.datakitchen.ide.model;

import java.util.EventObject;

public class NodeModelEvent extends EventObject {

    private String property;
    private Object oldValue;
    private Object newValue;
    private Test test;

    public NodeModelEvent(Object source, Test test) {
        super(source);
        this.test = test;
    }

    public NodeModelEvent(Object source, String property, Object oldValue, Object newValue){
        super(source);
        this.property = property;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    public Test getTest() {
        return test;
    }

    public String getProperty() {
        return property;
    }

    public Object getOldValue() {
        return oldValue;
    }

    public Object getNewValue() {
        return newValue;
    }
}
