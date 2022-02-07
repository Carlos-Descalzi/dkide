package io.datakitchen.ide.tree.simple;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.projectView.ProjectViewNode;
import com.intellij.ide.projectView.ViewSettings;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.InvalidVirtualFileAccessException;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.SimpleTextAttributes;
import io.datakitchen.ide.Constants;
import io.datakitchen.ide.ui.UIUtil;
import io.datakitchen.ide.util.RecipeUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;

public abstract class SimpleRecipeNode extends ProjectViewNode<VirtualFile> {

    public SimpleRecipeNode(Project project, VirtualFile item, ViewSettings settings) {
        super(project, item, settings);
    }
    public boolean canNavigateToSource(){
        return true;
    }

    public void navigate(boolean requestFocus){
        FileEditorManager.getInstance(getProject()).openFile(
            Objects.requireNonNull(getValue().findChild(Constants.FILE_NOTEBOOK_JSON)),true);
    }

    @Override
    public boolean contains(@NotNull VirtualFile file) {
        try {
            return getValue().equals(file) || Set.of(getValue().getChildren()).contains(file);
        }catch(InvalidVirtualFileAccessException ignored){
            // happens when node is removed
        }
        return false;
    }

    protected abstract String getIconName();

    public VirtualFile getVirtualFile() {
        return getValue().findChild(Constants.FILE_NOTEBOOK_JSON);
    }
    @Override
    public void update(@NotNull PresentationData presentation) {

        Module module = ModuleUtil.findModuleForFile(getValue(),getProject());

        Set<String> nodes = RecipeUtil.getActiveGraphNodes(module);

        boolean inactive = !(nodes.isEmpty() || nodes.contains(getValue().getName()));

        Icon icon = UIUtil.getNodeIcon(getProject(), getValue(), !inactive, UIUtil.PROJECT_TREE_ICON_SIZE);

        if (icon != null) {
            presentation.setIcon(icon);
        }
        if (inactive){
            presentation.addText(getValue().getName(), SimpleTextAttributes.GRAY_ATTRIBUTES);
        } else {
            presentation.addText(getValue().getName(),SimpleTextAttributes.REGULAR_ATTRIBUTES);
        }
    }

    @Override
    public @NotNull Collection<? extends AbstractTreeNode<?>> getChildren() {
        return new ArrayList<>();
    }
}
