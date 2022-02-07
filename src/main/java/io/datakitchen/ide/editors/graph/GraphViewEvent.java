package io.datakitchen.ide.editors.graph;

import java.util.EventObject;

public class GraphViewEvent extends EventObject {

    private final GraphModel.Node node;
    public GraphViewEvent(Object source, GraphModel.Node node) {
        super(source);
        this.node = node;
    }

    public GraphModel.Node getNode(){
        return node;
    }
}
