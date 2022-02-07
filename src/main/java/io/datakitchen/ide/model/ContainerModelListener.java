package io.datakitchen.ide.model;

import java.util.EventListener;

public interface ContainerModelListener extends EventListener {
    default void inputFilesAdded(ContainerModelEvent event){}
    default void inputFilesRemoved(ContainerModelEvent event){}
    default void variableAssignmentAdded(ContainerModelEvent event){}
    default void variableAssignmentRemoved(ContainerModelEvent event){}
    default void nodePropertyChanged(ContainerModelEvent event){}
    default void inputFileRenamed(ContainerModelEvent containerModelEvent){}
}
