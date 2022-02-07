package io.datakitchen.ide.editors.graph;

import java.util.EventObject;

public class GraphModelEvent extends EventObject {
    private GraphModel.Node node;
    private GraphModel.Edge edge;
    private String oldName;
    private String newName;

    public GraphModelEvent(Object source, GraphModel.Node node){
        super(source);
        this.node = node;
    }
    public GraphModelEvent(Object source, GraphModel.Edge edge){
        super(source);
        this.edge = edge;
    }

    public GraphModelEvent(Object source, GraphModel.Node node, String oldName, String newName) {
        super(source);
        this.node = node;
        this.oldName = oldName;
        this.newName = newName;
    }

    public GraphModel.Node getNode() {
        return node;
    }

    public GraphModel.Edge getEdge() {
        return edge;
    }

    public String getOldName() {
        return oldName;
    }

    public String getNewName() {
        return newName;
    }
}
