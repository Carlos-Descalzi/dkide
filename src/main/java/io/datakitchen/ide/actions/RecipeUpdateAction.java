package io.datakitchen.ide.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import io.datakitchen.ide.dialogs.InputMessageDialog;
import org.jetbrains.annotations.NotNull;

public class RecipeUpdateAction extends CLIAction{

    private String message;
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        InputMessageDialog dialog = new InputMessageDialog();
        if (dialog.showAndGet()) {
            message = dialog.getMessage();
            super.actionPerformed(e);
        }
    }

    @Override
    protected String[] getCommand(AnActionEvent e) {
        return new String[]{"dk","ru","-m",message};
    }
}
