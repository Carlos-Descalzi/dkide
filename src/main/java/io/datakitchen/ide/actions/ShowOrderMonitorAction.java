package io.datakitchen.ide.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.ui.content.impl.ContentImpl;
import io.datakitchen.ide.tools.OrderMonitor;
import io.datakitchen.ide.util.ToolWindowUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ShowOrderMonitorAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        OrderMonitor monitor = new OrderMonitor(e.getProject());

        ToolWindowUtil.show("orders",e.getProject(), List.of(new ContentImpl(monitor, "Orders", false)));
    }
}
