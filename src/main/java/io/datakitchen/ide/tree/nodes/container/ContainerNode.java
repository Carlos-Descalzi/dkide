package io.datakitchen.ide.tree.nodes.container;

import com.intellij.ide.projectView.ViewSettings;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiManager;
import io.datakitchen.ide.model.NodeType;
import io.datakitchen.ide.tree.BaseRecipeNodeFolder;
import io.datakitchen.ide.tree.nodes.AbstractContainerNode;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ContainerNode extends AbstractContainerNode {

    public ContainerNode(Project project, VirtualFile item, ViewSettings viewSettings) {
        super(project, item, viewSettings);
    }

    protected String getIconName(){
        return NodeType.CONTAINER_NODE_TYPE_NAME;
    }

    public @NotNull Collection<? extends AbstractTreeNode<?>> getChildren() {
        List<AbstractTreeNode<?>> items = new ArrayList<>();
        VirtualFile dockerShareFolder = getValue().findChild("docker-share");
        Project project = getProject();
        if (dockerShareFolder != null && project != null){
            PsiDirectory folder = PsiManager.getInstance(getProject()).findDirectory(dockerShareFolder);
            if (folder != null) {
                items.add(new BaseRecipeNodeFolder(getProject(), folder, getSettings()));
            }
        }
        items.addAll(super.getChildren());

        return items;
    }
}
