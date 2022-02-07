package io.datakitchen.ide.ui;

import com.intellij.openapi.editor.Editor;
import io.datakitchen.ide.editors.DocumentChangeEvent;
import io.datakitchen.ide.editors.DocumentChangeListener;
import io.datakitchen.ide.editors.DocumentEditor;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

public class FieldListener
        implements FocusListener,
        ActionListener,
        DocumentListener,
        ListSelectionListener,
        TableModelListener,
        com.intellij.openapi.editor.event.DocumentListener,
        DocumentChangeListener {
    /*
    A class which implements the listeners used for form fields, and end up invoking a given action
    when notifying changes on these fields.
    Just as a way to remove redundant code.
     */

    private final Runnable target;
    private boolean enabled;

    public FieldListener(Runnable target){
        this.target = target;
        this.enabled = true;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void listen(JTextField textField){
        textField.addFocusListener(this);
    }
    public void listen(JRadioButton radio){
        radio.addActionListener(this);
    }
    public void listen(JComboBox<?> combo){
        combo.addActionListener(this);
    }
    public void listen(JList<?> list){
        list.addListSelectionListener(this);
    }
    public void listen(JCheckBox checkBox) {
        checkBox.addActionListener(this);
    }
    public void listen(EntryField entryField) {
        entryField.addFocusListener(this);
    }
    public void listen(JTextArea textArea) {
        textArea.addFocusListener(this);
    }
    public void listen(JEditorPane textArea) {
        textArea.addFocusListener(this);
    }
    public void listen(JTable table) {
        table.getModel().addTableModelListener(this);
    }
    public void listen(Editor editor) {
        editor.getDocument().addDocumentListener(this);
    }
    public void listen(DocumentEditor editor) {
        editor.addDocumentChangeListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (enabled) {
            target.run();
        }
    }

    @Override
    public void focusGained(FocusEvent e) {

    }

    @Override
    public void focusLost(FocusEvent e) {
        if (enabled) {
            target.run();
        }
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        if (enabled) {
            target.run();
        }
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        if (enabled) {
            target.run();
        }
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        if (enabled) {
            target.run();
        }
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        if (enabled) {
            target.run();
        }
    }

    @Override
    public void tableChanged(TableModelEvent e) {
        if (enabled){
            target.run();
        }
    }

    @Override
    public void documentChanged(com.intellij.openapi.editor.event.@NotNull DocumentEvent event) {
        if (enabled){
            target.run();
        }
    }

    @Override
    public void documentChanged(DocumentChangeEvent e) {
        if (enabled){
            target.run();
        }
    }

    public void noListen(Runnable runnable){
        enabled = false;
        try {
            runnable.run();
        } finally {
            enabled = true;
        }
    }
}
