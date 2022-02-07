package io.datakitchen.ide.actions;

import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.content.impl.ContentImpl;
import io.datakitchen.ide.hooks.Action;
import io.datakitchen.ide.service.HookService;
import io.datakitchen.ide.tools.LogTargetImpl;
import io.datakitchen.ide.util.ToolWindowUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CustomToolsActionGroup extends ActionGroup {
    @Override
    public AnAction @NotNull [] getChildren(@Nullable AnActionEvent e) {
        if (e != null) {
            Project project = e.getProject();
            if (project != null) {
                return HookService.getInstance(project)
                        .getActions().stream()
                        .map(this::create)
                        .toArray(AnAction[]::new);
            }
        }
        return new AnAction[0];
    }

    private AnAction create(Action action){
        return new ActionWrapper(action);
    }

    private static class ActionWrapper extends AnAction {
        private final Action action;

        public ActionWrapper(Action action){
            this.action = action;
        }

        @Override
        public void update(@NotNull AnActionEvent e) {
            super.update(e);

            Module module = e.getData(LangDataKeys.MODULE);
            VirtualFile file = e.getData(LangDataKeys.VIRTUAL_FILE);

            boolean enabled = action.enabled(
                    module == null ? null : module.getName(),
                    file == null ? null : file.getPath());

            e.getPresentation().setText(action.getName());
            e.getPresentation().setDescription(action.getDescription());
            e.getPresentation().setEnabled(enabled);
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            Module module = e.getData(LangDataKeys.MODULE);
            VirtualFile file = e.getData(LangDataKeys.VIRTUAL_FILE);

            LogTargetImpl logTarget = new LogTargetImpl();

            action.setLogTarget(logTarget);

            ToolWindowUtil.show(
                "Run "+action.getName(),
                e.getProject(),
                List.of(new ContentImpl(logTarget, "Action Log", false))
            );

            action.run(
                module == null ? null : module.getName(),
                file == null ? null : file.getPath()
            );
        }
    }

}
