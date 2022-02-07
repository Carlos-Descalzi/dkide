package io.datakitchen.ide.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.module.Module;
import io.datakitchen.ide.dialogs.ImportNodeDialog;
import io.datakitchen.ide.util.RecipeUtil;
import org.jetbrains.annotations.NotNull;

public class ImportNodeAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Module module = e.getData(LangDataKeys.MODULE);
        ImportNodeDialog dialog = new ImportNodeDialog(e.getProject(), RecipeUtil.recipeFolder(module));
        if (dialog.showAndGet()){
            dialog.pullNode();
        }
    }
}
