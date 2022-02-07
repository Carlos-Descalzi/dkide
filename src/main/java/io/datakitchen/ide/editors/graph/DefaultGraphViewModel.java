package io.datakitchen.ide.editors.graph;

import com.intellij.openapi.util.IconLoader;

import javax.swing.*;

public class DefaultGraphViewModel implements GraphViewModel{

    private static final NodeAttributes DEFAULT_ATTR = new NodeAttributes();
    @Override
    public NodeAttributes getNodeAttributes(GraphModel.Node node) {
        return DEFAULT_ATTR;
    }

    @Override
    public Icon getIcon(GraphModel.Node node, String nodeType) {
        if (nodeType != null) {
            return IconLoader.getIcon("/icons/" + nodeType + "_big.svg",getClass());
        }
        return null;
    }
}
