package io.datakitchen.ide.dialogs;

import com.intellij.openapi.ui.DialogWrapper;
import io.datakitchen.ide.ui.FormPanel;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class InputMessageDialog extends DialogWrapper {
    private final FormPanel panel = new FormPanel();
    private final JTextField message = new JTextField();
    public InputMessageDialog(){
        super(true);
        panel.addField("Message", message, new Dimension(300,28));
        init();
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        return panel;
    }

    public String getMessage() {
        return message.getText();
    }
}
