package io.datakitchen.ide.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.ThrowableComputable;
import com.intellij.openapi.vfs.VirtualFile;
import io.datakitchen.ide.builder.ModuleBuilder;
import io.datakitchen.ide.config.Account;
import io.datakitchen.ide.dialogs.PullRecipeDialog;
import io.datakitchen.ide.module.Site;
import io.datakitchen.ide.platform.ServiceClient;
import io.datakitchen.ide.ui.UIUtil;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class PullRecipeAction extends AnAction {

    private static final Logger LOGGER = Logger.getInstance(PullRecipeAction.class);

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {

        PullRecipeDialog dialog = new PullRecipeDialog(e.getProject());
        Project project = e.getProject();

        if (dialog.showAndGet()){

            Site site = dialog.getSite();
            Account account = dialog.getAccount();
            String kitchen = dialog.getKitchenName();
            String recipe = dialog.getRecipeName();

            ServiceClient client = new ServiceClient(site.getUrl());

            UIUtil.showNotification(project,"Downloading recipe "+recipe+" ...","Download Recipe");

            try {
                client.login(account.getUsername(), account.getPassword());

                ThrowableComputable<VirtualFile, IOException> action = () ->
                        project.getBaseDir()
                                .createChildDirectory(PullRecipeAction.this, recipe);

                VirtualFile newRecipeFolder = ApplicationManager.getApplication().runWriteAction(action);

                new Thread(()->{
                    try {
                        client.pullRecipe(kitchen, recipe, newRecipeFolder);
                        makeModule(site, account, kitchen, e.getProject(), newRecipeFolder);
                        UIUtil.showNotification(project,"Recipe "+recipe+" downloaded successfully","Download Recipe");
                    }catch (Exception ex){
                        LOGGER.error(ex);
                    }
                }).start();

            }catch (Exception ex){
                LOGGER.error(ex);
            }
        }
    }

    private void makeModule(Site site, Account account, String kitchen, Project project, VirtualFile recipeFolder){
        ApplicationManager.getApplication().invokeLater(()->{
            ApplicationManager.getApplication().runWriteAction(()->
                    new ModuleBuilder(project)
                        .setRecipePath(recipeFolder.toNioPath())
                        .setProperty("site", site.getName())
                        .setProperty("account", account.getName())
                        .setProperty("kitchen", kitchen)
                        .build()
            );
        });
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setEnabled(e.getProject() != null);
    }
}
