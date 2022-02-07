package io.datakitchen.ide.ui;

import com.intellij.ui.components.JBScrollPane;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class TextAreaInlineEditor extends JBScrollPane implements InlineEditor {
    private final EventSupport<ActionListener> listeners = EventSupport.of(ActionListener.class);

    private final JTextArea textArea = new JTextArea();

    public TextAreaInlineEditor(){
        super(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        setViewportView(textArea);
        textArea.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER
                    && e.getModifiersEx() == 0){
                    notifyChange();
                }
            }
        });
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
        return textArea;
    }

    public String getText(){
        return textArea.getText();
    }

    public void setText(String text){
        this.textArea.setText(text);
    }

}
