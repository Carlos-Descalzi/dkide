package io.datakitchen.ide.ui;

import javax.swing.*;
import javax.swing.text.JTextComponent;

public class TextFieldInlineEditor extends JTextField implements InlineEditor {

    public TextFieldInlineEditor(){}

    public TextFieldInlineEditor(String text){
        setText(text);
    }

    @Override
    public JComponent getComponent() {
        return this;
    }

    @Override
    public JTextComponent getEditorComponent() {
        return this;
    }
}
