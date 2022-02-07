package io.datakitchen.ide.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import io.datakitchen.ide.config.Account;
import io.datakitchen.ide.config.ConfigurationService;
import io.datakitchen.ide.config.GlobalConfiguration;
import io.datakitchen.ide.dialogs.RecipeSettingsDialog;
import io.datakitchen.ide.module.Site;
import io.datakitchen.ide.platform.ServiceClient;
import io.datakitchen.ide.service.RecipeModuleSettingsService;
import io.datakitchen.ide.ui.UIUtil;
import io.datakitchen.ide.util.RecipeUtil;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

public class PushRecipeAction extends AnAction {

    private static final Logger LOGGER = Logger.getInstance(PushRecipeAction.class);

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {

        Module module = e.getData(LangDataKeys.MODULE);
        RecipeModuleSettingsService settingsService = RecipeModuleSettingsService.getInstance(module);

        String siteName = settingsService.getProperty("site");
        String accountName = settingsService.getProperty("account");
        String kitchen = settingsService.getProperty("kitchen");

        if (StringUtils.isBlank(siteName)){
            if (isCliRecipe(module)){
                Messages.showErrorDialog("This recipe has been downloaded with CLI, please use CLI Recipe update action to upload it", "Error");
                return;
            }
            RecipeSettingsDialog dialog = new RecipeSettingsDialog(e.getProject());

            if (!dialog.showAndGet()){
                return;
            }

            siteName = dialog.getSiteName();
            accountName = dialog.getAccountName();
            kitchen = dialog.getKitchenName();

            settingsService.setProperty("site",siteName);
            settingsService.setProperty("account",accountName);
            settingsService.setProperty("kitchen",kitchen);

        }

        pushRecipe(siteName, accountName, kitchen, module);

    }

    private void pushRecipe(String siteName, String accountName, String kitchenName, Module recipeModule){
        ConfigurationService service = ConfigurationService.getInstance(recipeModule.getProject());

        GlobalConfiguration globalConfiguration = service.getGlobalConfiguration();

        Site site = globalConfiguration.getAllSites().stream().filter(s -> s.getName().equals(siteName)).findFirst().orElse(null);

        if (site == null){
            Messages.showErrorDialog("Site "+siteName+" no longer exists in configuration","Error");
            return;
        }
        Account account = globalConfiguration.getAccounts().stream().filter(a -> a.getName().equals(accountName)).findFirst().orElse(null);

        if (account == null){
            Messages.showErrorDialog("Account "+accountName+" no longer exists in configuration","Error");
            return;
        }

        ServiceClient client = new ServiceClient(site.getUrl());

        VirtualFile recipeFolder = RecipeUtil.recipeFolder(recipeModule);

        new Thread(()->{
            try {
                UIUtil.showNotification(recipeModule.getProject(),"Recipe Upload in Progress", "Recipe "+recipeFolder.getName()+" is being uploaded ...");
                client.login(account.getUsername(), account.getPassword());

                client.pushRecipe(kitchenName, recipeFolder, true);
                UIUtil.showNotification(recipeModule.getProject(),"Recipe Upload Finished", "Recipe "+recipeFolder.getName()+" uploaded successfully.");
            }catch(Exception ex){
                LOGGER.error(ex);
                ApplicationManager.getApplication().invokeLater(()->{
                    Messages.showErrorDialog("Unable to push recipe: "+ex.getMessage(),"Error");
                });
            }
        }).start();
    }

    private boolean isCliRecipe(Module module) {
        VirtualFile recipeFolder = RecipeUtil.recipeFolder(module);
        return recipeFolder.getParent().findChild(".dk") != null;
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        Module module = e.getData(LangDataKeys.MODULE);

        e.getPresentation().setEnabled(
            module != null
            && !isCliRecipe(module)
        );
    }
}
