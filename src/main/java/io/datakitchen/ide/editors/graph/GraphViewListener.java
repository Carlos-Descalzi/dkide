package io.datakitchen.ide.editors.graph;

import java.util.EventListener;

public interface GraphViewListener extends EventListener {
    default void nodeSelected(GraphViewEvent event) {}
    default void nodeOpenRequested(GraphViewEvent event){}
}
