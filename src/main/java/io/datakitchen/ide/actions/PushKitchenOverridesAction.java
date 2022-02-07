package io.datakitchen.ide.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import io.datakitchen.ide.config.Account;
import io.datakitchen.ide.dialogs.PushPullKitchenOverridesDialog;
import io.datakitchen.ide.module.Site;
import io.datakitchen.ide.platform.OverridesPusher;
import io.datakitchen.ide.ui.UIUtil;
import io.datakitchen.ide.util.JsonUtil;
import io.datakitchen.ide.util.RecipeUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class PushKitchenOverridesAction extends AnAction {

    private static final Logger LOGGER = Logger.getInstance(PushKitchenOverridesAction.class);

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project != null) {
            PushPullKitchenOverridesDialog dialog = new PushPullKitchenOverridesDialog(e.getProject());
            if (dialog.showAndGet()) {

                Site site = dialog.getSite();
                Account account = dialog.getAccount();
                String kitchenName = dialog.getKitchenName();
                boolean overwrite = !dialog.isPreserveExisting();
                Map<String, Object> overrides = getOverrides(e.getProject());

                UIUtil.showNotificationSync(
                    project,
                    "Storing overrides",
                    "Storing overrides for kitchen "+kitchenName);

                new OverridesPusher(site, account, kitchenName, overwrite)
                    .run(
                        overrides,
                        ()-> SwingUtilities.invokeLater(()->{
                            UIUtil.showNotificationSync(
                                project,
                                "Overrides stored successfully",
                                "Overrides stored in kitchen "+kitchenName
                            );
                        }),
                        ex -> SwingUtilities.invokeLater(() -> {
                            Messages.showErrorDialog("Unable to push kitchen overrides:" + ex.getMessage(), "Error");
                        })
                    );

            }
        }
    }

    private Map<String, Object> getOverrides(Project project) {
        try {
            VirtualFile overridesFile = RecipeUtil.getLocalOverridesFile(project);

            return JsonUtil.read(overridesFile);
        }catch(Exception ex){
            LOGGER.error(ex);
            return new LinkedHashMap<>();
        }
    }
}
