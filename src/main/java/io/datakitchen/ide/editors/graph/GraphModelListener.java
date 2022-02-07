package io.datakitchen.ide.editors.graph;

import java.util.EventListener;

public interface GraphModelListener extends EventListener {
    void nodeAdded(GraphModelEvent event);
    void edgeAdded(GraphModelEvent event);
    void nodeRemoved(GraphModelEvent event);
    void edgeRemoved(GraphModelEvent event);
    void nodeChanged(GraphModelEvent graphModelEvent);
}
