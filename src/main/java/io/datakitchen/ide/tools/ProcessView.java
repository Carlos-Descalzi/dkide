package io.datakitchen.ide.tools;

import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.process.ProcessListener;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.Key;
import io.datakitchen.ide.ui.SimpleAction;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class ProcessView extends JPanel implements Disposable {

    private final ProcessHandler process;
    private final Project project;
    private final Runnable onFinish;
    private final JLabel status = new JLabel();
    private final Action stopAction = new SimpleAction("Stop", this::stopProcess);

    public ProcessView(Project project, ProcessHandler process, Runnable onFinish){
        this.process = process;
        this.project = project;
        this.onFinish = onFinish;

        setLayout(new BorderLayout());

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JButton(stopAction));
        topPanel.add(status);
        status.setText("Running ...");
        status.setIcon(AllIcons.General.Gear);
        add(topPanel, BorderLayout.NORTH);
        ConsoleView console = TextConsoleBuilderFactory.getInstance().createBuilder(project).getConsole();
        Disposer.register(this, console);
        add(console.getComponent(), BorderLayout.CENTER);
        console.attachToProcess(process);
        process.addProcessListener(new ProcessListener() {
            @Override
            public void startNotified(@NotNull ProcessEvent event) {}

            @Override
            public void onTextAvailable(@NotNull ProcessEvent event, @NotNull Key outputType) {}

            @Override
            public void processTerminated(@NotNull ProcessEvent event) {
                stopAction.setEnabled(false);
                status.setText("Process finished with exit code "+event.getExitCode());
                if (event.getExitCode() == 0){
                    status.setIcon(AllIcons.General.InspectionsOK);
                } else {
                    status.setIcon(AllIcons.General.Error);
                }
                if (onFinish != null){
                    ApplicationManager.getApplication().invokeLater(onFinish);
                }
            }
        });
    }

    @Override
    public void dispose() {

    }

    private void stopProcess(ActionEvent event) {
        stopAction.setEnabled(false);
        process.destroyProcess();
    }

}
