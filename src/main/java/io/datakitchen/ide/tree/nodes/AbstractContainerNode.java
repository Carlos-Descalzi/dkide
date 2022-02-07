package io.datakitchen.ide.tree.nodes;

import com.intellij.ide.projectView.ViewSettings;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import io.datakitchen.ide.tree.sinks.DataSinksNode;
import io.datakitchen.ide.tree.sources.DataSourcesNode;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class AbstractContainerNode extends AbstractNode {

    public AbstractContainerNode(Project project, VirtualFile item, ViewSettings viewSettings) {
        super(project, item, viewSettings);
    }

    public @NotNull Collection<? extends AbstractTreeNode<?>> getChildren() {
        List<AbstractTreeNode<?>> items = new ArrayList<>(super.getChildren());

        PsiDirectory dataSourcesFolder = psiFolder(getProject(), getValue(), "data_sources");
        if (dataSourcesFolder != null) {
            DataSourcesNode dataSources = new DataSourcesNode(getProject(), this, dataSourcesFolder, "Data Sources", getSettings());
            items.add(dataSources);
        }
        PsiDirectory dataSinksFolder = psiFolder(getProject(), getValue(), "data_sinks");
        if (dataSinksFolder != null) {
            DataSinksNode dataSinks = new DataSinksNode(getProject(), this, dataSinksFolder, getSettings());
            items.add(dataSinks);
        }

        return items;
    }
}
