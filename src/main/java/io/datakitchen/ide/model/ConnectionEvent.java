package io.datakitchen.ide.model;

import java.util.EventObject;

public class ConnectionEvent extends EventObject {

    private Test test;
    private RuntimeVariable variable;
    private Key key;
    private String oldName;
    private String newName;

    public ConnectionEvent(Object source) {
        super(source);
    }
    public ConnectionEvent(Object source, Test test) {
        super(source);
        this.test = test;
    }

    public ConnectionEvent(Object source, RuntimeVariable variable) {
        super(source);
        this.variable = variable;
    }

    public ConnectionEvent(Object source, Key key) {
        super(source);
        this.key = key;
    }

    public ConnectionEvent(Object source, String oldName, String newName){
        super(source);
        this.oldName = oldName;
        this.newName = newName;
    }

    public Key getKey() {
        return key;
    }

    public Test getTest() {
        return test;
    }

    public RuntimeVariable getVariable() {
        return variable;
    }

    public String getOldName() {
        return oldName;
    }

    public String getNewName() {
        return newName;
    }
}
