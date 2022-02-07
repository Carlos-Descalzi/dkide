package io.datakitchen.ide.actions;

import com.intellij.ide.projectView.ProjectView;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import io.datakitchen.ide.config.ConfigurationService;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class ToggleNodeVisibilityAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {

        ConfigurationService service = ConfigurationService.getInstance(Objects.requireNonNull(e.getProject()));

        service.getGlobalConfiguration().getMiscOptions().setHideInactiveNodes(
                !service
                    .getGlobalConfiguration()
                    .getMiscOptions()
                    .isHideInactiveNodes());

        service.saveGlobalConfiguration();

        ProjectView.getInstance(e.getProject()).refresh();
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        super.update(e);
        e.getPresentation().setEnabled(e.getData(LangDataKeys.MODULE) != null);
    }
}
