package io.datakitchen.ide.tree.nodes;

import com.intellij.ide.projectView.ViewSettings;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;

public class ConditionalNode extends AbstractNode{

    public ConditionalNode(Project project, VirtualFile item, ViewSettings viewSettings) {
        super(project, item, viewSettings);
    }

    @Override
    public @NotNull Collection<? extends AbstractTreeNode<?>> getChildren() {
        return new ArrayList<>();
    }

    protected String getIconName(){
        return "ConditionalNode";
    }
}
