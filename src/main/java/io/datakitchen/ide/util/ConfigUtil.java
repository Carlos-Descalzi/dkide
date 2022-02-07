package io.datakitchen.ide.util;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import io.datakitchen.ide.config.ConfigurationService;
import io.datakitchen.ide.config.GlobalConfiguration;
import io.datakitchen.ide.dialogs.FirstTimeSetupDialog;
import io.datakitchen.ide.dialogs.GlobalConfigurationDialog;
import io.datakitchen.ide.service.ContainerService;

import java.util.ArrayList;

public class ConfigUtil {
    public static void editGlobalConfiguration(Project project){
        editGlobalConfiguration(project, false);
    }
    public static void editGlobalConfiguration(Project project, boolean forcePullImages){
        ApplicationManager.getApplication().invokeLater(()->{
            ConfigurationService service = project.getService(ConfigurationService.class);

            GlobalConfigurationDialog dialog = new GlobalConfigurationDialog();

            GlobalConfiguration config = service.getGlobalConfiguration();

            dialog.setGlobalConfiguration(config);

            if (dialog.showAndGet()){
                dialog.saveGlobalConfiguration(config);
                service.setGlobalConfiguration(config);

                ContainerService containerService = ContainerService.getInstance(project);

                boolean available = containerService.checkServiceAvailable();

                if (available && (forcePullImages || dialog.shouldPullImages())){
                    DockerUtil.pullImages(project);
                }
            }
        });
    }

    public static void editFirstTimeSetup(Project project) {
        FirstTimeSetupDialog dialog = new FirstTimeSetupDialog();

        ConfigurationService service = project.getService(ConfigurationService.class);
        GlobalConfiguration config = service.getGlobalConfiguration();
        if (config == null){
            config = new GlobalConfiguration();
        }

        dialog.setGlobalConfiguration(config);

        if (dialog.showAndGet()){
            dialog.saveGlobalConfiguration(config);
            config.setSites(new ArrayList<>());
            config.setSecrets(new ArrayList<>());
            config.setConnections(new ArrayList<>());
            config.setAccounts(new ArrayList<>());

            service.setGlobalConfiguration(config);

            ContainerService containerService = ContainerService.getInstance(project);

            boolean available = containerService.checkServiceAvailable();

            if (available){
                DockerUtil.pullImages(project);
            }
        }
    }
}
