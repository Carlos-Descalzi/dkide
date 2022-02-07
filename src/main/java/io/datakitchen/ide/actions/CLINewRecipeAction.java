package io.datakitchen.ide.actions;

import com.intellij.ide.projectView.ProjectView;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import io.datakitchen.ide.builder.ModuleBuilder;
import io.datakitchen.ide.dialogs.CLINewRecipeDialog;
import io.datakitchen.ide.util.CommandRunner;
import io.datakitchen.ide.util.RecipeUtil;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;

public class CLINewRecipeAction extends AnAction {

    private static final Logger LOGGER = Logger.getInstance(CLINewRecipeAction.class);

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {

        Project project = e.getProject();

        CLINewRecipeDialog dialog = new CLINewRecipeDialog();

        if (dialog.showAndGet()) {
            String kitchenPath = dialog.getKitchenPath();
            String recipeName = dialog.getRecipeName();

            VirtualFile kitchenFolder = VirtualFileManager.getInstance().findFileByNioPath(Path.of(kitchenPath));

            if (kitchenFolder != null) {
                final CommandRunner runner = new CommandRunner(project);
                final String windowId = "create recipe " + recipeName;

                runner.run(kitchenFolder, new String[]{"dk", "re", recipeName}, windowId, () -> {
                    runner.run(kitchenFolder, new String[]{"dk", "rg", recipeName}, windowId, () -> {
                        addRecipe(project, kitchenFolder, recipeName);
                    });
                });
            } else {
                LOGGER.error("Kitchen folder for path "+kitchenPath+" was null");
            }
        }
    }

    private void addRecipe(Project project, VirtualFile kitchenFolder, String recipeName) {

        Path recipeFolder = Path.of(kitchenFolder.getPath(), recipeName);

        ApplicationManager.getApplication().runWriteAction(()-> {
            try {
                RecipeUtil.setupNewRecipe(project, recipeFolder);
            } catch (IOException ex){
                LOGGER.error(ex);
            }
            new ModuleBuilder(project)
                    .setRecipePath(recipeFolder)
                    .build();
        });
        ProjectView.getInstance(project).refresh();
    }

}
