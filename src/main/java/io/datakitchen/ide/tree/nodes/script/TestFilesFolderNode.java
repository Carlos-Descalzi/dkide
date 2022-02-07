package io.datakitchen.ide.tree.nodes.script;

import com.intellij.ide.projectView.ViewSettings;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import io.datakitchen.ide.tree.BaseRecipeNodeFolder;

public class TestFilesFolderNode extends BaseRecipeNodeFolder {

    public TestFilesFolderNode(Project project, PsiDirectory folder, ViewSettings viewSettings) {
        super(project, folder, viewSettings);
    }

    public String getDisplayName(){
        return "Test files";
    }

}
