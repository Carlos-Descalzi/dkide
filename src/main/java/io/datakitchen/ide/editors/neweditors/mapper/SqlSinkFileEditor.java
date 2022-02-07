package io.datakitchen.ide.editors.neweditors.mapper;

import io.datakitchen.ide.ui.EventSupport;
import io.datakitchen.ide.ui.InlineEditor;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

class SqlSinkFileEditor extends JPanel implements InlineEditor {
    private final EventSupport<ActionListener> listeners = EventSupport.of(ActionListener.class);
    private final JTextField field = new JTextField();
    private final JLabel extension = new JLabel();

    public SqlSinkFileEditor(String text) {
        setPreferredSize(new Dimension(250, 28));
        setLayout(new BorderLayout());
        add(field, BorderLayout.CENTER);
        add(extension, BorderLayout.EAST);
        setText(text);
        field.addActionListener(e -> notifyChange());
    }

    private void notifyChange() {
        listeners.getProxy().actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED,""));
    }

    @Override
    public void addActionListener(ActionListener listener) {
        this.listeners.addListener(listener);
    }
    @Override
    public JComponent getComponent() {
        return this;
    }

    @Override
    public JTextComponent getEditorComponent() {
        return field;
    }

    @Override
    public String getText() {
        return field.getText()+extension.getText();
    }

    @Override
    public void setText(String text) {
        String name = text.substring(0, text.lastIndexOf('.'));
        String extension = text.substring(text.lastIndexOf('.'));
        this.field.setText(name);
        this.extension.setText(extension);
    }

}
