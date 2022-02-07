package io.datakitchen.ide.tree.sinks;

import com.intellij.ide.projectView.ViewSettings;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import io.datakitchen.ide.tree.BaseRecipeNodeFolder;
import io.datakitchen.ide.tree.nodes.AbstractNode;
import org.jetbrains.annotations.NotNull;

public class DataSinksNode extends BaseRecipeNodeFolder {

    public DataSinksNode(Project project, AbstractNode parent, @NotNull PsiDirectory value, ViewSettings viewSettings) {
        super(project, value, viewSettings);
    }

    public String toString(){
        return "Data Sinks";
    }

    public String getName(){
        return this.toString();
    }

}
