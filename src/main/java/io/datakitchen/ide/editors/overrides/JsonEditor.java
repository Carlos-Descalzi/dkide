package io.datakitchen.ide.editors.overrides;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.module.Module;
import io.datakitchen.ide.ui.EditorUtil;
import io.datakitchen.ide.ui.EventSupport;
import io.datakitchen.ide.util.JsonUtil;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import java.awt.*;

public class JsonEditor extends JPanel implements ValueEditor, Disposable {

    private final EventSupport<ChangeListener> listeners = EventSupport.of(ChangeListener.class);
    private final Editor editor;

    public JsonEditor(Module module){
        setLayout(new BorderLayout());
        editor = EditorUtil.createJsonEditor(module.getProject());
        add(editor.getComponent(), BorderLayout.CENTER);
    }

    @Override
    public void dispose() {
        EditorFactory.getInstance().releaseEditor(editor);
    }

    @Override
    public void addChangeListener(ChangeListener listener) {
        this.listeners.addListener(listener);
    }

    @Override
    public void removeChangeListener(ChangeListener listener) {
        this.listeners.removeListener(listener);
    }

    @Override
    public Object getValue() {
        String text = editor.getDocument().getText();
        try {
            return JsonUtil.read(text);
        }catch(Exception ex){
            ex.printStackTrace();
            return null;
        }
    }

    @Override
    public void setValue(Object value) {
        String text = value == null ? "null" : JsonUtil.toJsonString(value);
        EditorUtil.setText(editor, text);
    }

    @Override
    public JComponent getComponent() {
        return this;
    }

}
