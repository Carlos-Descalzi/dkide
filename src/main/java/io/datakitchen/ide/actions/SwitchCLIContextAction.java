package io.datakitchen.ide.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import io.datakitchen.ide.dialogs.SwitchCLIContextDialog;
import org.jetbrains.annotations.NotNull;

public class SwitchCLIContextAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {

        SwitchCLIContextDialog dialog = new SwitchCLIContextDialog();
        if (dialog.showAndGet()){
            dialog.switchContext();
        }
    }
}
