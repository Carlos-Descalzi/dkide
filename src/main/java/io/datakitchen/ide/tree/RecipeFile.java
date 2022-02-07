package io.datakitchen.ide.tree;

import com.intellij.ide.projectView.ViewSettings;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;

public class RecipeFile extends BaseRecipeFileNode {

    public RecipeFile(Project project, PsiFile item, ViewSettings settings) {
        super(project, item, settings);
    }

    @Override
    public boolean isAlwaysLeaf() {
        return true;
    }

}
