package io.datakitchen.ide.tree.simple;

import com.intellij.ide.projectView.ViewSettings;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import io.datakitchen.ide.model.NodeType;

public class SimpleContainerNode extends SimpleRecipeNode {

    public SimpleContainerNode(Project project, VirtualFile item, ViewSettings settings) {
        super(project, item, settings);
    }

    protected String getIconName(){
        return NodeType.CONTAINER_NODE_TYPE_NAME;
    }

}
