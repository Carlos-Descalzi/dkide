package io.datakitchen.ide.tree;

import com.intellij.ide.projectView.ViewSettings;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;

public class ResourcesFolderNode extends BaseRecipeNodeFolder {
    public ResourcesFolderNode(Project project, PsiDirectory folder, ViewSettings viewSettings) {
        super(project, folder, viewSettings);
    }

}
