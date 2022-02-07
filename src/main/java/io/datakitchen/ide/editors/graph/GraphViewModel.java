package io.datakitchen.ide.editors.graph;

import com.intellij.openapi.util.IconLoader;

import javax.swing.*;

public interface GraphViewModel {

    NodeAttributes getNodeAttributes(GraphModel.Node node);

    default String getDescription(GraphModel.Node node){
        return null;
    }

    default Icon getIcon(GraphModel.Node node, String nodeType) {
        if (nodeType != null) {
            return IconLoader.getIcon("/icons/" + nodeType + "_big.svg",getClass());
        }
        return null;
    }
}
