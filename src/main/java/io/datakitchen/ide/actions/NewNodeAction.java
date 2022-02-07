package io.datakitchen.ide.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.module.Module;
import io.datakitchen.ide.builder.NodeBuilder;
import io.datakitchen.ide.dialogs.NewNodeDialog;
import org.jetbrains.annotations.NotNull;

public class NewNodeAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        NewNodeDialog dialog = new NewNodeDialog();
        if (dialog.showAndGet()){
            String nodeName = dialog.getNodeName();
            String nodeType = dialog.getNodeType();
            Module module = e.getData(LangDataKeys.MODULE);

            new NodeBuilder(e.getProject())
                .setModule(module)
                .setNodeName(nodeName)
                .setNodeType(nodeType)
                .build(null);
        }

    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        Module module = e.getData(LangDataKeys.MODULE);
        e.getPresentation().setEnabled(module != null);
    }
}
