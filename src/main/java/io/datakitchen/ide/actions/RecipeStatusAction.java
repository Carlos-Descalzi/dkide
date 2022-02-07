package io.datakitchen.ide.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;

public class RecipeStatusAction extends CLIAction{
    @Override
    protected String[] getCommand(AnActionEvent e) {
        return new String[]{"dk","rs"};
    }
}
