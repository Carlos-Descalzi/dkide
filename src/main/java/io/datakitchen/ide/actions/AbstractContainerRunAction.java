package io.datakitchen.ide.actions;

import com.intellij.notification.Notification;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.impl.ContentImpl;
import io.datakitchen.ide.config.ConfigurationService;
import io.datakitchen.ide.config.Secret;
import io.datakitchen.ide.service.Container;
import io.datakitchen.ide.service.ContainerDefinition;
import io.datakitchen.ide.service.ContainerService;
import io.datakitchen.ide.service.ContainerServiceException;
import io.datakitchen.ide.tools.ContainerRunView;
import io.datakitchen.ide.ui.UIUtil;
import io.datakitchen.ide.util.ToolWindowUtil;
import org.apache.commons.io.FileUtils;

import javax.swing.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;

public abstract class AbstractContainerRunAction extends ActionWithHooks {

    protected Supplier<Long> getDiskUsageSupplier(VirtualFile path){
        return () -> FileUtils.sizeOfDirectory(new File(path.getPath()));

    }

    protected void runContainer(
            String title,
            Project project,
            ContainerDefinition containerDefinition,
            Runnable onFinish,
            Supplier<Long> diskUsageSupplier,
            Content ... additionalContent
    ){
        Notification msg = UIUtil.showNotificationSync(project,
            "Launching container",
            "");

        ApplicationManager.getApplication().invokeLater(()->{
            String cliId = title.toLowerCase(Locale.ROOT).replace(" ","_");

            List<Content> contents = new ArrayList<>();

            try {
                ContainerService.getInstance(project).createContainerAsync(containerDefinition,(Container container)->{
                    SwingUtilities.invokeLater(()->{
                        contents.add(new ContentImpl(new ContainerRunView(project, container, onFinish, diskUsageSupplier), title, false));
                        contents.addAll(Arrays.asList(additionalContent));

                        SwingUtilities.invokeLater(()->{
                            ToolWindowUtil.show(cliId, project, contents);
                            msg.hideBalloon();
                        });
                    });
                });
            }catch(ContainerServiceException ex){
                Messages.showErrorDialog(ex.getMessage(),"Error");
            }
        });
    }

    protected List<Secret> getSecrets(Project project){
        ConfigurationService service = project.getService(ConfigurationService.class);
        return service.getSecrets();
    }
}
