package io.datakitchen.ide.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import io.datakitchen.ide.run.VariationRunner;
import io.datakitchen.ide.service.ContainerService;
import io.datakitchen.ide.util.RecipeUtil;
import org.jetbrains.annotations.NotNull;

public class RunVariationAction extends AbstractContainerRunAction {

    private static final Logger LOGGER = Logger.getInstance(RunVariationAction.class);

    public void doRunAction(@NotNull AnActionEvent e) {

        Module module = e.getData(LangDataKeys.MODULE);
        String activeVariation = RecipeUtil.getActiveVariation(module);

        if (module == null){
            LOGGER.error("Module was null");
            return;
        }

        ApplicationManager.getApplication().invokeLater(()->{
            new VariationRunner(module)
                    .setVariation(activeVariation)
                    .run();
        });

    }

    @Override
    public void update(@NotNull AnActionEvent e) {

        Project project = e.getProject();

        boolean serviceAvailable = project != null
                && ContainerService.getInstance(project).isServiceAvailable();

        Module module = e.getData(LangDataKeys.MODULE);
        String activeVariation = RecipeUtil.getActiveVariation(module);
        e.getPresentation().setEnabled(serviceAvailable && module != null && activeVariation != null);
    }
}
