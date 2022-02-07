package io.datakitchen.ide.ui;

import java.util.EventObject;

public class ItemListEvent extends EventObject {
    private final NamedObject item;
    private final String oldName;

    public ItemListEvent(Object source, NamedObject item, String oldName){
        super(source);
        this.item = item;
        this.oldName = oldName;
    }

    public ItemListEvent(Object source, NamedObject item){
        this(source, item, null);
    }

    public NamedObject getItem() {
        return item;
    }

    public String getOldName(){
        return oldName;
    }
}
