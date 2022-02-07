package io.datakitchen.ide.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import io.datakitchen.ide.util.DockerUtil;
import org.jetbrains.annotations.NotNull;

public class UpdateImagesAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        DockerUtil.pullImages(e.getProject());
    }
}
