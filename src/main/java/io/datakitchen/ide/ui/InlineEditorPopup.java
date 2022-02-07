package io.datakitchen.ide.ui;

import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.function.Consumer;
import java.util.function.Function;

public class InlineEditorPopup {

    public static void edit(JComponent hook, InlineEditor editor, Consumer<String> onFInish){
        edit(hook, editor, onFInish, s -> true);
    }

    public static void edit(JComponent hook, InlineEditor editor, Consumer<String> onFInish, Function<String, Boolean> validator){
        JComponent component = editor.getComponent();
        JTextComponent editorComponent = editor.getEditorComponent();
        if (!editorComponent.isPreferredSizeSet()) {
            Dimension size = hook.getSize();
            size.height+=5;
            editorComponent.setPreferredSize(size);
        }
        JBPopup popup = JBPopupFactory
                .getInstance()
                .createComponentPopupBuilder(component, editorComponent)
                .setFocusable(true)
                .setRequestFocus(true)
                .createPopup();
        editor.addActionListener((ActionEvent e)->{
            onFInish.accept(editor.getText());
            popup.cancel();
        });
        popup.showInScreenCoordinates(hook, hook.getLocationOnScreen());
    }
}
