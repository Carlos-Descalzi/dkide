package io.datakitchen.ide.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import io.datakitchen.ide.dialogs.PullSecretsDialog;
import org.jetbrains.annotations.NotNull;

public class PullSecretsAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        PullSecretsDialog dialog = new PullSecretsDialog();
        if (dialog.showAndGet()){
            dialog.doPull(e.getProject());
        }
    }
}
