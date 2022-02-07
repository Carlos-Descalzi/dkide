package io.datakitchen.ide.debugger;

import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.process.ProcessOutputType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import io.datakitchen.ide.service.Container;
import io.datakitchen.ide.service.ContainerService;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

public class ContainerProcessHandler extends ProcessHandler {
    private Container container;
    private Project project;
    private Thread thread;
    private final ContainerService containerService;
    private final Runnable onFinish;
    private String lastLog;


    public ContainerProcessHandler(Project project, Runnable onFinish) {
        this.project = project;
        this.containerService = ContainerService.getInstance(project);
        this.onFinish = onFinish;
    }

    public void setContainer(Container container) {
        this.container = container;
        if (this.container != null) {
            this.thread = new Thread(this::checkProcess);
            this.thread.start();
        }
    }

    @Override
    protected void destroyProcessImpl() {
        if (container != null) {
            try {
                containerService.stopContainer(container);
            } catch (Exception ignored) {
            }
        }
    }

    private void checkProcess() {
        while (containerService.isRunning(container)) {
            notifyLogs();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                break;
            }
        }
        notifyLogs();
        int exitCode = containerService.getExitCode(container);
        System.out.println("Container finished");
        notifyProcessTerminated(exitCode);
        if (onFinish != null){
            onFinish.run();
        }
    }

    private void notifyLogs() {
        String logs = containerService.getLogs(container, null);

        if (StringUtils.isNotBlank(logs)) {
            if (lastLog == null) {
                notifyTextAvailable(logs, ProcessOutputType.STDOUT);
            } else {
                if (logs.length() > lastLog.length()){
                    notifyTextAvailable(logs.substring(lastLog.length()), ProcessOutputType.STDOUT);
                }
            }
            lastLog = logs;
        }
    }

    @Override
    public <T> T getUserData(@NotNull Key<T> key) {
        System.out.println("GET USER DATA:"+key);
        return super.getUserData(key);
    }

    @Override
    protected void detachProcessImpl() {

    }

    @Override
    public boolean detachIsDefault() {
        return false;
    }

    @Override
    public @Nullable OutputStream getProcessInput() {
        return new ByteArrayOutputStream(0);
    }

}
