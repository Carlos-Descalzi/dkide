package io.datakitchen.ide.actions;

import com.intellij.ide.projectView.ProjectView;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import io.datakitchen.ide.config.ConfigurationService;
import io.datakitchen.ide.config.ProjectConfiguration;
import io.datakitchen.ide.dialogs.ProjectConfigurationDialog;
import org.jetbrains.annotations.NotNull;

public class EditProjectConfigurationAction extends AnAction {


    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {

        Project project = e.getProject();

        if (project != null) {
            ConfigurationService service = ConfigurationService.getInstance(project);

            ProjectConfigurationDialog dialog = new ProjectConfigurationDialog();

            ProjectConfiguration config = service.getProjectConfiguration();
            dialog.setProjectConfiguration(config);

            if (dialog.showAndGet()) {
                dialog.saveProjectConfiguration(config);
                service.setProjectConfiguration(config);
            }

            ProjectView.getInstance(project).refresh();

        }
    }
}
