package io.datakitchen.ide.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.module.Module;
import io.datakitchen.ide.util.RecipeUtil;

public class CreateOrderAction extends CLIAction{
    @Override
    protected String[] getCommand(AnActionEvent e) {
        Module module = e.getData(LangDataKeys.MODULE);
        String variation = RecipeUtil.getActiveVariation(module);
        return new String[]{"dk","or","-w","-y",variation};
    }
}
