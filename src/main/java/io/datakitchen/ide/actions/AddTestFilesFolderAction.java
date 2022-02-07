package io.datakitchen.ide.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.vfs.VirtualFile;
import io.datakitchen.ide.util.RecipeUtil;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class AddTestFilesFolderAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {

        VirtualFile folder = e.getData(LangDataKeys.VIRTUAL_FILE);

        if (folder != null) {
            ApplicationManager.getApplication().runWriteAction(() -> {
                try {
                    folder.createChildDirectory(this, "test-files");
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            });
        }
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setEnabled(RecipeUtil.isScriptNode(e.getData(LangDataKeys.VIRTUAL_FILE)));
    }
}
