package io.datakitchen.ide.editors.neweditors.container;

import com.intellij.openapi.ui.DialogWrapper;
import io.datakitchen.ide.ui.FormPanel;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

public class FileSinkEditor extends DialogWrapper {

    private final JTextField containerFile = new JTextField();
    private final JTextField sinkFile = new JTextField();

    protected FileSinkEditor() {
        super(true);
        init();
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        FormPanel panel = new FormPanel(new Dimension(300,28));
        JPanel containerFilePanel = new JPanel(new BorderLayout());
        containerFilePanel.add(new JLabel("docker-share/"), BorderLayout.WEST);
        containerFilePanel.add(containerFile, BorderLayout.CENTER);
        panel.addField("Container file",containerFilePanel);
        panel.addField("Sink file",sinkFile);
        containerFile.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                onContainerFileFocusLost();
            }
        });
        return panel;
    }

    private void onContainerFileFocusLost() {
        if (StringUtils.isBlank(sinkFile.getText())){
            sinkFile.setText(containerFile.getText());
        }
    }

    public String getContainerFile(){
        return containerFile.getText();
    }

    public String getSinkFile(){
        return sinkFile.getText();
    }
}
