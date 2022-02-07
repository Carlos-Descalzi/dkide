package io.datakitchen.ide.tree.sources;

import com.intellij.ide.projectView.ViewSettings;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import io.datakitchen.ide.tree.BaseRecipeNodeFolder;
import io.datakitchen.ide.tree.nodes.AbstractNode;
import org.jetbrains.annotations.NotNull;

public class DataSourcesNode extends BaseRecipeNodeFolder {

    private final String name;

    public DataSourcesNode(Project project, AbstractNode parent, @NotNull PsiDirectory value, String name, ViewSettings viewSettings) {
        super(project, value, viewSettings);
        this.name = name;
    }

    public String toString(){
        return name;
    }

    public String getName(){
        return name;
    }

}
