package io.datakitchen.ide;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManagerListener;
import com.intellij.openapi.ui.Messages;
import io.datakitchen.ide.config.ConfigurationService;
import io.datakitchen.ide.service.ContainerService;
import io.datakitchen.ide.util.ConfigUtil;
import org.jetbrains.annotations.NotNull;

public class ProjectListener implements ProjectManagerListener {

    private static final Logger LOGGER = Logger.getInstance(ProjectListener.class);

    @Override
    public void projectOpened(@NotNull Project project) {

        if (!ConfigurationService.getInstance(project).globalConfigurationExists()){
//            ConfigUtil.editGlobalConfiguration(project, true);
            ConfigUtil.editFirstTimeSetup(project);
        }
        try {
            ContainerService.getInstance(project).startService();
        } catch (Exception e) {
            LOGGER.error(e);
        }
        if (!ContainerService.getInstance(project).isServiceAvailable()){
            Messages.showErrorDialog(
                    "Docker service seems to not be running in your computer, some features will be disabled", "Error"
            );
        }
    }

    @Override
    public void projectClosed(@NotNull Project project) {
    }

    @Override
    public void projectClosing(@NotNull Project project) {
    }

    @Override
    public void projectClosingBeforeSave(@NotNull Project project) {
    }
}
