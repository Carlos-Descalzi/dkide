package io.datakitchen.ide.tree.nodes;

import com.intellij.ide.projectView.ViewSettings;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import io.datakitchen.ide.model.NodeType;
import io.datakitchen.ide.tree.sources.DataSourcesNode;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ActionNode extends AbstractNode {

    public ActionNode(Project project, VirtualFile item, ViewSettings viewSettings) {
        super(project, item, viewSettings);
    }

    public @NotNull Collection<? extends AbstractTreeNode<?>> getChildren() {
        List<AbstractTreeNode<?>> items = new ArrayList<>();
        PsiDirectory dir = psiFolder(getProject(), getValue(), "actions");
        if (dir != null){
            items.add(new DataSourcesNode(getProject(), this,dir, "Actions", getSettings()));
        }
        items.addAll(super.getChildren());
        return items;
    }


}
