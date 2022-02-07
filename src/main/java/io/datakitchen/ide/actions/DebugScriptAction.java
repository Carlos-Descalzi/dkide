package io.datakitchen.ide.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import io.datakitchen.ide.Constants;
import io.datakitchen.ide.run.ScriptDebugger;
import io.datakitchen.ide.service.ContainerService;
import io.datakitchen.ide.util.RecipeUtil;
import org.jetbrains.annotations.NotNull;

public class DebugScriptAction extends ActionWithHooks {


    @Override
    public void doRunAction(@NotNull AnActionEvent e) {

        Module module = e.getData(LangDataKeys.MODULE);
        VirtualFile scriptFile = getSelectedScript(e);

        new ScriptDebugger(module, scriptFile)
            .run();
    }


    protected VirtualFile getSelectedScript(AnActionEvent e){
        VirtualFile file = e.getData(LangDataKeys.VIRTUAL_FILE);

        if (file != null
                && !file.isDirectory()
                && file.getName().endsWith(".py")
                && file.getParent().getName().equals(Constants.DOCKER_SHARE_FOLDER_NAME)){
            return file;
        }
        return null;
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        Project project = e.getProject();

        boolean serviceAvailable = project != null
                && ContainerService.getInstance(project).isServiceAvailable();

        VirtualFile script = getSelectedScript(e);
        Module module = e.getData(LangDataKeys.MODULE);
        String activeVariation = RecipeUtil.getActiveVariation(module);

        e.getPresentation().setEnabled(
                serviceAvailable
                && ScriptDebugger.isDebugEnabled()
                && script != null
                && RecipeUtil.isScriptNode(script.getParent().getParent())
                && activeVariation != null
        );
    }


}
