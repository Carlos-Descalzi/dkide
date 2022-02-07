package io.datakitchen.ide.tree.nodes;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.projectView.ProjectViewNode;
import com.intellij.ide.projectView.ViewSettings;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiManager;
import com.intellij.ui.SimpleTextAttributes;
import io.datakitchen.ide.Constants;
import io.datakitchen.ide.tree.RecipeFile;
import io.datakitchen.ide.ui.UIUtil;
import io.datakitchen.ide.util.RecipeUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public abstract class AbstractNode extends ProjectViewNode<VirtualFile> {

    private String nodeType;

    public AbstractNode(Project project, VirtualFile item, ViewSettings viewSettings) {
        super(project, item, viewSettings);
    }

    @Override
    public boolean contains(@NotNull VirtualFile file) {
        return VfsUtilCore.isAncestor(getValue(), file, false);
    }

    protected String getIconName(){
        return null;
    }

    @Override
    protected void update(@NotNull PresentationData presentation) {
        String iconName = getIconName();

        assert getProject() != null;
        Module module = ModuleUtil.findModuleForFile(getValue(),getProject());

        Set<String> nodes = RecipeUtil.getActiveGraphNodes(module);

        boolean inactive = !(nodes.isEmpty() || nodes.contains(getValue().getName()));

        if (iconName != null) {
            presentation.setIcon(IconLoader.getIcon("/icons/" + iconName + (inactive ? "_disabled" : "") + ".svg", getClass()));
        } else {
            Icon icon = UIUtil.getNodeIcon(getProject(), getValue(), !inactive, UIUtil.PROJECT_TREE_ICON_SIZE);
            if (icon != null){
                presentation.setIcon(icon);
            }
        }

        if (inactive){
            presentation.addText(getValue().getName(),SimpleTextAttributes.GRAY_ATTRIBUTES);
        } else {
            presentation.addText(getValue().getName(),SimpleTextAttributes.REGULAR_ATTRIBUTES);
        }
    }

    @Override
    public @NotNull Collection<? extends AbstractTreeNode<?>> getChildren() {
        //return Arrays.asList(testsNode);
        List<AbstractTreeNode<?>> items = new ArrayList<>();

        assert getProject() != null;
        PsiManager mgr = PsiManager.getInstance(getProject());

        VirtualFile description = getValue().findChild(Constants.FILE_DESCRIPTION_JSON);
        assert description != null;
        items.add(new RecipeFile(getProject(), mgr.findFile(description), getSettings()));

        VirtualFile notebook = getValue().findChild(Constants.FILE_NOTEBOOK_JSON);

        if (notebook != null){
            items.add(new RecipeFile(getProject(), mgr.findFile(notebook), getSettings()));
        }

        VirtualFile iconFile = getValue().findChild("icon.svg");
        if (iconFile != null){
            items.add(new RecipeFile(getProject(), mgr.findFile(iconFile), getSettings()));
        }

        return items;
    }

    protected PsiDirectory psiFolder(Project project, VirtualFile parent, String folder){
        VirtualFile child = parent.findChild(folder);

        if (child != null) {
            return PsiManager.getInstance(project).findDirectory(child);
        }
        return null;
    }

}
