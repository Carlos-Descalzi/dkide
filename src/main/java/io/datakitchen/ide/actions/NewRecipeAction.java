package io.datakitchen.ide.actions;

import com.intellij.ide.projectView.ProjectView;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.ThrowableComputable;
import com.intellij.openapi.vfs.VirtualFile;
import io.datakitchen.ide.builder.ModuleBuilder;
import io.datakitchen.ide.dialogs.NewRecipeDialog;
import io.datakitchen.ide.util.RecipeUtil;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.OutputStream;

public class NewRecipeAction extends AnAction {

    private static final Logger LOGGER = Logger.getInstance(NewRecipeAction.class);

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        NewRecipeDialog dialog = new NewRecipeDialog();

        if (dialog.showAndGet()){
            String recipeName = dialog.getRecipeName();

            Project project = e.getProject();
            VirtualFile projectFolder = project.getBaseDir();

            ThrowableComputable<Void, IOException> action = ()-> {
                doCreateRecipe(project, recipeName, projectFolder);
                return null;
            };

            try {
                ApplicationManager.getApplication().runWriteAction(action);
            }catch(IOException ex){
                Messages.showErrorDialog("Unable to create recipe: "+ex.getMessage(),"Error");
            }
        }
    }

    private void doCreateRecipe(Project project, String recipeName, VirtualFile projectFolder) throws IOException {
        VirtualFile recipeFolder = projectFolder.createChildDirectory(this, recipeName);

        for (String fileName: new String[]{"description.json","variations.json","variables.json","README.md"}){
            VirtualFile file = recipeFolder.createChildData(this, fileName);

            try (OutputStream output = file.getOutputStream(this)) {
                IOUtils.copy(getClass().getResourceAsStream("/templates/"+fileName), output);
            }
        }

        RecipeUtil.setupNewRecipe(project, recipeFolder);

        recipeFolder.createChildDirectory(this,"resources");

        new ModuleBuilder(project)
            .setRecipePath(recipeFolder.toNioPath())
            .build();

        ApplicationManager.getApplication().invokeLater(()->{
            ProjectView.getInstance(project).refresh();
        });
    }

}
