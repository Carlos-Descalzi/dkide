package io.datakitchen.ide.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import io.datakitchen.ide.run.DataSourceRunner;
import io.datakitchen.ide.service.ContainerService;
import io.datakitchen.ide.util.RecipeUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class RunDataSourceAction extends ActionWithHooks {

    private static final Set<String> DS_FOLDERS = new HashSet<>(Arrays.asList("data_sources","actions"));

    public void doRunAction(@NotNull AnActionEvent e) {

        Module module = e.getData(LangDataKeys.MODULE);
        VirtualFile sourceFile = e.getData(LangDataKeys.VIRTUAL_FILE);

        new DataSourceRunner(module, sourceFile)
            .run();
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
            && DS_FOLDERS.contains(path.getParent().getName());

        e.getPresentation().setEnabled(enabled);
    }
}
