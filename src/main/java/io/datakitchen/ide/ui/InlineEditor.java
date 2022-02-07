package io.datakitchen.ide.ui;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.event.ActionListener;

public interface InlineEditor {
    JComponent getComponent();
    JTextComponent getEditorComponent();
    String getText();
    void setText(String text);

    void addActionListener(ActionListener listener);
}
