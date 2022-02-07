package io.datakitchen.ide.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.module.Module;
import io.datakitchen.ide.util.CommandRunner;
import io.datakitchen.ide.util.RecipeUtil;
import org.jetbrains.annotations.NotNull;

public abstract class CLIAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Module module = e.getData(LangDataKeys.MODULE);

        String[] command = getCommand(e);

        String cmdString = String.join(" ",command);

        CommandRunner runner = new CommandRunner(e.getProject());
        runner.run(RecipeUtil.recipeFolder(module), command, cmdString);
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        super.update(e);
        e.getPresentation().setEnabled(e.getData(LangDataKeys.MODULE) != null);
    }

    protected abstract String[] getCommand(AnActionEvent e);

}
