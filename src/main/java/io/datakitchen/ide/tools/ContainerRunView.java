package io.datakitchen.ide.tools;

import com.intellij.execution.impl.ConsoleViewImpl;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.ui.components.JBTabbedPane;
import io.datakitchen.ide.service.Container;
import io.datakitchen.ide.service.ContainerService;
import io.datakitchen.ide.service.ContainerStats;
import io.datakitchen.ide.ui.MultilineChart;
import io.datakitchen.ide.ui.RegExValidatedField;
import io.datakitchen.ide.ui.SimpleAction;
import io.datakitchen.ide.util.NumberUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.function.Supplier;

public class ContainerRunView extends JPanel implements Disposable {

    private final Project project;
    private final Container container;
    private final ConsoleView consoleView;

    private final Action stopAction = new SimpleAction("Stop", this::stopContainer);
    private final JLabel status = new JLabel();
    private final MultilineChart chart = new MultilineChart(new String[]{"Memory","CPU","Storage"});
    private final Runnable onFinish;
    private final Supplier<Long> diskUsageSupplier;
    private final RegExValidatedField maxSamples = new RegExValidatedField(RegExValidatedField.NUMBER);

    public ContainerRunView(Project project, Container container, Runnable onFinish, Supplier<Long> diskUsageSupplier){
        this.project = project;
        this.container = container;
        this.onFinish = onFinish;
        this.diskUsageSupplier = diskUsageSupplier;
        consoleView = new ConsoleViewImpl(project, true);
        Disposer.register(this, consoleView);

        setLayout(new BorderLayout());
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JButton(stopAction));
        topPanel.add(status);
        status.setText("Running ...");
        status.setIcon(AllIcons.General.Gear);

        JTabbedPane tabs = new JBTabbedPane();
        JPanel consoleContainer = new JPanel(new BorderLayout());
        SwingUtilities.invokeLater(()->{
            consoleContainer.add(consoleView.getComponent(), BorderLayout.CENTER);
        });
        tabs.add("Output", consoleContainer);
        tabs.add("Metrics",buildMetricsPanel());

        add(topPanel, BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);
        Thread logThread = new Thread(this::getLogs);
        logThread.start();
        Thread statsThread = new Thread(this::getStats);
        statsThread.start();
    }

    @Override
    public void dispose() {
    }

    private Component buildMetricsPanel() {
        java.text.SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        chart.setXAxisSize(getFontMetrics(getFont()).stringWidth("00:00:00"));
        chart.setXLabelRenderer((Date s)-> sdf.format(s));
        chart.setYLabelRenderer((Integer i, Float f)->{
            if (i == 0 || i == 2){
                return NumberUtil.formatInBytes((long)f.floatValue());
            }
            return ((int)f.floatValue())+" %";
        });
        chart.setScaleCalculator((series, valueList) -> {
            float[] scales = new float[series];
            for (int i=0;i<series;i++){
                scales[i] = 0;
            }
            for (float[] values:valueList){
                for (int i=0;i<series;i++){
                    scales[i] = Math.max(scales[i],values[i]);
                }
            }
            for (int i=0;i<series;i++){
                if (i == 0 || i == 2) {
                    double ceil = Math.ceil(Math.log(scales[i]) / Math.log(2));
                    scales[i] = (float) Math.pow(2, ceil);
                } else {
                    scales[i] = 100;
                }
            }
            return scales;
        });
        JPanel panel = new JPanel(new BorderLayout());

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        topPanel.add(new JLabel("Max. visible samples:"));
        topPanel.add(maxSamples);
        maxSamples.setPreferredSize(new Dimension(40,28));
        maxSamples.setText("30");
        maxSamples.addActionListener(this::maxSamplesChanged);
        panel.add(topPanel,BorderLayout.NORTH);
        panel.add(chart, BorderLayout.CENTER);

        return panel;
    }

    private void maxSamplesChanged(ActionEvent event) {
        chart.setMaxVisibleValues(Integer.parseInt(maxSamples.getText()));
    }

    private void stopContainer(ActionEvent event) {
        ApplicationManager.getApplication().invokeLater(()->{
            try {
                ContainerService.getInstance(project).stopContainer(container);
            }catch(Exception ex){
            }
        });
    }

    private void getLogs(){
        ContainerService service = ContainerService.getInstance(project);
        service.getLogsStream(container, (String log)->{
            SwingUtilities.invokeLater(()->{
                consoleView.print(log, ConsoleViewContentType.NORMAL_OUTPUT);
            });
        }, ()->{
            stopAction.setEnabled(false);
            SwingUtilities.invokeLater(()->{
                int exitCode = service.getExitCode(container);
                status.setText("Container finished with exit code "+exitCode);
                if (exitCode == 0){
                    status.setIcon(AllIcons.General.InspectionsOK);
                } else {
                    status.setIcon(AllIcons.General.Error);
                }

            });
            if (onFinish != null){
                onFinish.run();
            }
        });
    }

    private void getStats(){
        ContainerService service = ContainerService.getInstance(project);
        service.getStatsStream(container, (ContainerStats stats)->{
            SwingUtilities.invokeLater(()->{
                Long memoryUsage = stats.getMemoryUsage();
                if (memoryUsage == null) {
                    memoryUsage = 0L;
                }
                Float cpuUsage = stats.getCpuUsage();
                if (cpuUsage == null){
                    cpuUsage = 0f;
                }
                Long diskUsage = null;
                if (diskUsageSupplier != null){
                    diskUsage = diskUsageSupplier.get();
                }
                if (diskUsage == null){
                    diskUsage = 0L;
                }
                chart.addValues(
                    new Date(), new float[]{
                            memoryUsage,
                            cpuUsage,
                            diskUsage
                    });
            });
        });

    }


}
