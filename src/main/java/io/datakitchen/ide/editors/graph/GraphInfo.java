package io.datakitchen.ide.editors.graph;

import java.awt.datatransfer.DataFlavor;
import java.io.Serializable;

public class GraphInfo implements Serializable {
    public static final DataFlavor FLAVOR = new DataFlavor(GraphInfo.class, "Graph");

    private final String recipeName;
    private final VariationGraph graph;

    public GraphInfo(String recipeName, VariationGraph graph) {
        this.recipeName = recipeName;
        this.graph = graph;
    }

    public String getRecipeName() {
        return recipeName;
    }

    public VariationGraph getGraph() {
        return graph;
    }
}
