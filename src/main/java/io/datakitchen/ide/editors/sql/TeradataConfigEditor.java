package io.datakitchen.ide.editors.sql;

import com.intellij.openapi.module.Module;
import io.datakitchen.ide.editors.DocumentChangeEvent;
import io.datakitchen.ide.editors.DocumentChangeListener;
import io.datakitchen.ide.editors.DocumentEditor;
import io.datakitchen.ide.ui.EntryField;
import io.datakitchen.ide.ui.EventSupport;
import io.datakitchen.ide.ui.FieldListener;
import io.datakitchen.ide.ui.FormPanel;
import org.apache.commons.lang.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

public class TeradataConfigEditor extends FormPanel implements DocumentEditor {

    private final EventSupport<DocumentChangeListener> eventSupport = EventSupport.of(DocumentChangeListener.class);

    private final EntryField username;
    private final EntryField password;
    private final EntryField hostname;

    private final FieldListener listener = new FieldListener(this::notifyChange);

    public TeradataConfigEditor(Module module){
        super(new Dimension(300,28));
        username = new EntryField(module);
        password = new EntryField(module);
        hostname = new EntryField(module);

        addField("Username", username);
        addField("Password", password);
        addField("Hostname", hostname);

        listener.listen(username);
        listener.listen(password);
        listener.listen(hostname);
    }

    public void setEnabled(boolean enabled){
        super.setEnabled(enabled);
        username.setEnabled(enabled);
        password.setEnabled(enabled);
        hostname.setEnabled(enabled);
        listener.setEnabled(enabled);
    }

    public void loadConfiguration(Map<String, Object> config) {
        username.setText(StringUtils.defaultString((String)config.get("username"),""));
        password.setText(StringUtils.defaultString((String)config.get("password"),""));
        hostname.setText(StringUtils.defaultString((String)config.get("hostname"),""));
    }

    public void saveConfiguration(Map<String, Object> config) {
        config.put("username", username.getText());
        config.put("password", password.getText());
        config.put("hostname", hostname.getText());
    }

    private void notifyChange() {
        this.eventSupport.getProxy().documentChanged(new DocumentChangeEvent(this));
    }

    @Override
    public void addDocumentChangeListener(DocumentChangeListener listener) {
        this.eventSupport.addListener(listener);
    }

    @Override
    public void removeDocumentChangeListener(DocumentChangeListener listener) {
        this.eventSupport.removeListener(listener);
    }

    @Override
    public JComponent getEditorComponent() {
        return this;
    }
}
