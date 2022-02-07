package io.datakitchen.ide.tree.nodes;

import com.intellij.ide.projectView.ViewSettings;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import io.datakitchen.ide.tree.sinks.DataSinksNode;
import io.datakitchen.ide.tree.sources.DataSourcesNode;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DataMapperNode extends AbstractNode {

    public DataMapperNode(Project project, VirtualFile item, ViewSettings viewSettings) {
        super(project, item,viewSettings);
    }

    public @NotNull Collection<? extends AbstractTreeNode<?>> getChildren() {

        List<AbstractTreeNode<?>> items = new ArrayList<>(super.getChildren());

        Project project = getProject();
        VirtualFile value = getValue();
        ViewSettings settings = getSettings();

        items.add(new DataSourcesNode(project, this,psiFolder(project, value, "data_sources"), "Data Sources", settings));
        items.add(new DataSinksNode(project, this, psiFolder(project, value, "data_sinks"), settings));

        return items;
    }

}
