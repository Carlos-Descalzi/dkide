package io.datakitchen.ide.model;

import java.util.EventObject;

public class KeyEvent extends EventObject {

    private RuntimeVariable variable;

    public KeyEvent(Object source){
        super(source);
    }

    public KeyEvent(Object source, RuntimeVariable variable){
        super(source);
        this.variable = variable;
    }

    public RuntimeVariable getVariable() {
        return variable;
    }
}
