package io.datakitchen.ide.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import io.datakitchen.ide.util.RecipeUtil;
import org.jetbrains.annotations.NotNull;

public class OpenDefaultEditorAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        VirtualFile file = e.getData(LangDataKeys.VIRTUAL_FILE);

        Project project = e.getProject();

        if (project != null && file != null) {
            FileEditorManager mgr = FileEditorManager.getInstance(e.getProject());

            ApplicationManager.getApplication().putUserData(RecipeUtil.CUSTOM_EDITOR_ENABLED, false);

            mgr.closeFile(file);
            mgr.openFile(file, true);

            ApplicationManager.getApplication().putUserData(RecipeUtil.CUSTOM_EDITOR_ENABLED, true);
        }
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        super.update(e);
        VirtualFile file = e.getData(LangDataKeys.VIRTUAL_FILE);
        e.getPresentation().setEnabled(e.getProject() != null && file != null && file.getName().endsWith(".json"));
    }
}
