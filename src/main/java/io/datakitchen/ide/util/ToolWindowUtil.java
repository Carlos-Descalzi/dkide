package io.datakitchen.ide.util;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.wm.RegisterToolWindowTask;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;

import java.util.List;

public class ToolWindowUtil {

    public static void show(String id, Project project, List<Content> contents){
        ToolWindowManager mgr = ToolWindowManager.getInstance(project);

        ToolWindow window = mgr.getToolWindow(id);

        if (window == null) {
            window = mgr.registerToolWindow(
                    RegisterToolWindowTask.closable(id, IconLoader.getIcon("/icons/dk-icon.png", ToolWindowUtil.class)));
            window.setTitle(id);
        } else {
            window.getContentManager().removeAllContents(true);
        }
        for (Content aContent: contents) {
            window.getContentManager().addContent(aContent);
        }
        window.show();
    }

}
