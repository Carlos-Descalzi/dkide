package io.datakitchen.ide.tree.simple;

import com.intellij.ide.projectView.ViewSettings;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import io.datakitchen.ide.model.NodeType;

public class SimpleActionNode extends SimpleRecipeNode {

    public SimpleActionNode(Project project, VirtualFile item, ViewSettings settings) {
        super(project, item, settings);
    }

    protected String getIconName(){
        return NodeType.ACTION_NODE_TYPE_NAME;
    }

}
