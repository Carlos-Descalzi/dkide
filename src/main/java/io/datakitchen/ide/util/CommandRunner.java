package io.datakitchen.ide.util;

import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.impl.ContentImpl;
import io.datakitchen.ide.tools.ProcessView;

import java.util.ArrayList;
import java.util.List;

public class CommandRunner {
    private final Project project;

    public CommandRunner(Project project){
        this.project = project;
    }

    public void run(VirtualFile cwd, String[] command, String cliId) {
        run(cwd, command, cliId, null);
    }

    public void run(VirtualFile cwd, String[] command, String cliId, Runnable onFinish) {
        GeneralCommandLine cli = new GeneralCommandLine(command);
        cli.setWorkDirectory(cwd.getPath());

        try {
            ProcessHandler handler = new OSProcessHandler(cli);
            ProcessView processView = new ProcessView(project, handler, ()->{
                if (onFinish != null){
                    onFinish.run();
                }
            });
            List<Content> contents = new ArrayList<>();
            contents.add(new ContentImpl(processView, "Process Output",false));
            ToolWindowUtil.show(cliId, project, contents);
            handler.startNotify();
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }
}
