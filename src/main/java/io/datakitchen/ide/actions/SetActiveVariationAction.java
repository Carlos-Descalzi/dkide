package io.datakitchen.ide.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.module.Module;
import io.datakitchen.ide.dialogs.SetActiveVariationDialog;
import org.jetbrains.annotations.NotNull;

public class SetActiveVariationAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {

        Module module = e.getData(LangDataKeys.MODULE);

        if (module != null) {

            SetActiveVariationDialog dialog = new SetActiveVariationDialog(e.getProject(), module);
            if (dialog.showAndGet()) {
                dialog.save();
            }
        }
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        super.update(e);
        e.getPresentation().setEnabled(e.getData(LangDataKeys.MODULE) != null);
    }
}
