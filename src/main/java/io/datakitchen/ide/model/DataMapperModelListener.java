package io.datakitchen.ide.model;

import java.util.EventListener;

public interface DataMapperModelListener extends EventListener {
    void mappingsAdded(DataMapperModelEvent event);
    void mappingsRemoved(DataMapperModelEvent event);
    default void nodePropertyChanged(DataMapperModelEvent event){}
}
