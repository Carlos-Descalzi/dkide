package io.datakitchen.ide.model;

import java.util.EventListener;

public interface NodeModelListener extends EventListener {

    default void testAdded(NodeModelEvent actionNodeModelEvent){}

    default void testRemoved(NodeModelEvent actionNodeModelEvent){}

    default void nodePropertyChanged(NodeModelEvent description){}
}
