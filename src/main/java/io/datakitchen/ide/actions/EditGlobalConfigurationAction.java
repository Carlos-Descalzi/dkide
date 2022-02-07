package io.datakitchen.ide.actions;

import com.intellij.ide.projectView.ProjectView;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import io.datakitchen.ide.util.ConfigUtil;
import org.jetbrains.annotations.NotNull;

public class EditGlobalConfigurationAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();

        if (project != null) {
            ConfigUtil.editGlobalConfiguration(project);
            ProjectView.getInstance(project).refresh();
        }
    }
}
