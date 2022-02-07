package io.datakitchen.ide.editors;

import java.util.EventObject;

public class DocumentChangeEvent extends EventObject {
    public DocumentChangeEvent(Object source){
        super(source);
    }
}
