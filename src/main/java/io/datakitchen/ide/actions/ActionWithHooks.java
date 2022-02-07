package io.datakitchen.ide.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.module.Module;
import io.datakitchen.ide.hooks.ActionHook;
import io.datakitchen.ide.service.HookService;
import io.datakitchen.ide.util.RecipeUtil;
import org.jetbrains.annotations.NotNull;

public abstract class ActionWithHooks extends AnAction {
    public void actionPerformed(@NotNull AnActionEvent e) {

        if (runPreActionHooks(e)) {
            doRunAction(e);
        }

        runPostActionHooks(e);

    }

    private void runPostActionHooks(AnActionEvent e){
        String actionId = e.getActionManager().getId(this);

        ActionHook hook = HookService.getInstance(e.getProject()).getHookForAction(actionId);

        Module module = e.getData(LangDataKeys.MODULE);
        String path = RecipeUtil.recipeFolder(module).getPath();

        if (hook != null){
            hook.after(module.getName(), path);
        }
    }

    private boolean runPreActionHooks(AnActionEvent e){
        String actionId = e.getActionManager().getId(this);

        ActionHook hook = HookService.getInstance(e.getProject()).getHookForAction(actionId);

        Module module = e.getData(LangDataKeys.MODULE);
        String path = RecipeUtil.recipeFolder(module).getPath();

        if (hook != null){
            return hook.before(module.getName(), path);
        }
        return true;
    }

    protected abstract void doRunAction(@NotNull AnActionEvent e);
}
