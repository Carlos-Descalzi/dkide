package io.datakitchen.ide.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import io.datakitchen.ide.builder.ModuleBuilder;
import io.datakitchen.ide.dialogs.AddRecipeDialog;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

public class AddRecipeAction extends AnAction {


    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        AddRecipeDialog dialog = new AddRecipeDialog();
        if (dialog.showAndGet()){
            String path = dialog.getPath();
            ApplicationManager.getApplication().runWriteAction(()->{
                Path folderPath = Path.of(path);
                new ModuleBuilder(project)
                    .setRecipePath(folderPath)
                    .build();
            });
        }
    }


}
