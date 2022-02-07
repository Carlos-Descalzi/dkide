package io.datakitchen.ide.model;

import java.util.EventObject;

public class ScriptNodeModelEvent extends EventObject {
    private ScriptNodeKey key;

    public ScriptNodeModelEvent(Object source, ScriptNodeKey key){
        super(source);
        this.key = key;
    }

    public ScriptNodeModelEvent(Object source){
        super(source);
    }

    public ScriptNodeKey getKey() {
        return key;
    }
}
