package io.datakitchen.ide.editors;

import java.util.EventObject;

public class VariationEditionEvent extends EventObject {

    private String typeName;
    private String itemName;
    private String oldItemName;

    public VariationEditionEvent(Object source, String typeName, String itemName) {
        super(source);
        this.typeName = typeName;
        this.itemName = itemName;
    }

    public VariationEditionEvent(Object source, String typeName, String itemName, String oldItemName) {
        super(source);
        this.typeName = typeName;
        this.itemName = itemName;
        this.oldItemName = oldItemName;
    }

    public String getTypeName() {
        return typeName;
    }

    public String getItemName() {
        return itemName;
    }

    public String getOldItemName() {
        return oldItemName;
    }
}
