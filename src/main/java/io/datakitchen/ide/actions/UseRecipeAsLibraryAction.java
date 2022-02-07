package io.datakitchen.ide.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import io.datakitchen.ide.service.LibraryService;
import io.datakitchen.ide.util.RecipeUtil;
import org.jetbrains.annotations.NotNull;

public class UseRecipeAsLibraryAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        VirtualFile recipeFolder = e.getData(LangDataKeys.VIRTUAL_FILE);

        if (project != null && recipeFolder != null) {
            LibraryService.getInstance(project).addRecipe(recipeFolder);
        }
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        super.update(e);

        VirtualFile file = e.getData(LangDataKeys.VIRTUAL_FILE);
        Module module = e.getData(LangDataKeys.MODULE);

        e.getPresentation().setEnabled(
            file != null
            && RecipeUtil.isRecipeFolder(module, file)
        );
    }
}
