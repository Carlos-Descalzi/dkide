package io.datakitchen.ide.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import io.datakitchen.ide.dialogs.PushPullKitchenOverridesDialog;
import org.jetbrains.annotations.NotNull;

public class PullKitchenOverridesAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        PushPullKitchenOverridesDialog dialog = new PushPullKitchenOverridesDialog(e.getProject());
        if (dialog.showAndGet()){
            dialog.pullOverrides();
        }
    }
}
