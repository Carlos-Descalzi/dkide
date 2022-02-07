package io.datakitchen.ide.editors.neweditors.container;

import com.intellij.openapi.ui.DialogWrapper;
import io.datakitchen.ide.ui.FormPanel;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

public class FileSourceEditor extends DialogWrapper {

    private final JTextField containerFile = new JTextField();
    private final JTextField sourceFile = new JTextField();

    protected FileSourceEditor() {
        super(true);
        init();
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        FormPanel panel = new FormPanel(new Dimension(300,28));
        JPanel containerFilePanel = new JPanel(new BorderLayout());
        containerFilePanel.add(new JLabel("docker-share/"), BorderLayout.WEST);
        containerFilePanel.add(containerFile, BorderLayout.CENTER);
        panel.addField("Source file",sourceFile);
        panel.addField("Container file",containerFilePanel);
        sourceFile.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                onContainerFileFocusLost();
            }
        });
        return panel;
    }

    private void onContainerFileFocusLost() {
        if (StringUtils.isBlank(containerFile.getText())){
            containerFile.setText(sourceFile.getText());
        }
    }

    public String getContainerFile(){
        return containerFile.getText();
    }

    public String getSourceFile(){
        return sourceFile.getText();
    }
}
