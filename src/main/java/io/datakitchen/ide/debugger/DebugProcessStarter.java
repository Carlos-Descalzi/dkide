package io.datakitchen.ide.debugger;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.xdebugger.XDebugProcess;
import com.intellij.xdebugger.XDebugProcessStarter;
import com.intellij.xdebugger.XDebugSession;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class DebugProcessStarter extends XDebugProcessStarter {
    private final Project project;
    private final List<VirtualFile> sourceFolders;
    private final String containerBasePath;
    private final ProcessHandler processHandler;

    public DebugProcessStarter(Project project, List<VirtualFile> sourceFolders, String containerBasePath, ProcessHandler processHandler) {
        this.project = project;
        this.sourceFolders = sourceFolders;
        this.containerBasePath = containerBasePath;
        this.processHandler = processHandler;
    }

    @Override
    public @NotNull XDebugProcess start(@NotNull XDebugSession session) throws ExecutionException {
        return new PythonDebugProcess(session, project, sourceFolders, containerBasePath, processHandler);
    }
}
