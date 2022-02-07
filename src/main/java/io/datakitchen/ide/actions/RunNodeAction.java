package io.datakitchen.ide.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import io.datakitchen.ide.Constants;
import io.datakitchen.ide.run.NodeRunner;
import io.datakitchen.ide.service.ContainerService;
import io.datakitchen.ide.util.RecipeUtil;
import org.jetbrains.annotations.NotNull;

public class RunNodeAction extends AbstractContainerRunAction {

    public void doRunAction(@NotNull AnActionEvent e) {

        ApplicationManager.getApplication().invokeLater(()->{
            Module module = e.getData(LangDataKeys.MODULE);
            VirtualFile nodePath = e.getData(LangDataKeys.VIRTUAL_FILE);

            new NodeRunner(module, nodePath)
                    .run();
        });
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        Project project = e.getProject();

        boolean serviceAvailable = project != null
                && ContainerService.getInstance(project).isServiceAvailable();

        VirtualFile path = e.getData(LangDataKeys.VIRTUAL_FILE);
        Module module = e.getData(LangDataKeys.MODULE);
        String activeVariation = RecipeUtil.getActiveVariation(module);

        boolean enabled = serviceAvailable
                && path != null
                && activeVariation != null
                && ( ( path.isDirectory()
                        && path.findChild(Constants.FILE_DESCRIPTION_JSON) != null
                        && path.findChild(Constants.FILE_VARIATIONS_JSON) == null
                    ) || (!path.isDirectory() && path.getName().equals(Constants.FILE_NOTEBOOK_JSON)));

        e.getPresentation().setEnabled(enabled);
    }
}
