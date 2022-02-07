package io.datakitchen.ide.editors.graph;

import io.datakitchen.ide.ui.NamedObject;
import io.datakitchen.ide.util.ObjectUtil;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class VariationGraph implements NamedObject, Transferable {
    private String recipeName;
    private String name;
    private List<List<Object>> graph;
    private final transient ConditionsCollection conditions; // TODO make it not transient and copy this

    public VariationGraph(String recipeName, String name, List<List<Object>> graph, ConditionsCollection conditions) {
        this.recipeName = recipeName;
        this.name = name;
        this.graph = graph;
        this.conditions = conditions;
    }

    public VariationGraph() {
        this(null, null, new ArrayList<>(), null);
    }

    public VariationGraph(String name) {
        this(null, name, new ArrayList<>(), null);
    }

    public String getRecipeName() {
        return recipeName;
    }

    public void setRecipeName(String recipeName) {
        this.recipeName = recipeName;
    }

    public String toString() {
        return StringUtils.isBlank(name) ? "(no name)" : name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ConditionsCollection getConditions() {
        return conditions;
    }

    public List<List<Object>> getGraph() {
        return graph;
    }

    public void setGraph(List<List<Object>> graph) {
        this.graph = graph;
    }

    public static VariationGraph fromEntry(String recipeName, Map.Entry<String, Object> entry, ConditionsCollection conditions) {
        return new VariationGraph(
                recipeName,
                entry.getKey(),
                ObjectUtil.cast(entry.getValue()),
                conditions
        );
    }

    public void renameNode(String oldValue, String newValue) {
        List<List<Object>> newGraph = new ArrayList<>();

        for (List<Object> edge: graph){
            List<Object> newEdge = new ArrayList<>();
            for (Object node:edge){
                if (node.equals(oldValue)){
                    newEdge.add(newValue);
                } else {
                    newEdge.add(node);
                }
            }
            newGraph.add(newEdge);
        }

        graph = newGraph;
    }

    public void removeNode(String name) {
        List<List<Object>> newGraph = new ArrayList<>();

        for (List<Object> edge: graph){
            List<Object> newEdge = new ArrayList<>();
            for (Object node:edge){
                if (!node.equals(name)){
                    newEdge.add(node);
                }
            }
            if (newEdge.size() > 0) {
                newGraph.add(newEdge);
            }
        }

        graph = newGraph;
    }

    @Override
    public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[]{GraphInfo.FLAVOR};
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return GraphInfo.FLAVOR.equals(flavor);
    }

    @NotNull
    @Override
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
        if (!GraphInfo.FLAVOR.equals(flavor)){
            throw new UnsupportedFlavorException(flavor);
        }
        try {
            return new GraphInfo(recipeName, ObjectUtil.copy(this));
        }catch (Exception ex){
            throw new IOException(ex);
        }
    }
}
