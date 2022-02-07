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

public class VerticaConfigEditor extends FormPanel implements DocumentEditor {

    private final EventSupport<DocumentChangeListener> eventSupport = EventSupport.of(DocumentChangeListener.class);

    private final EntryField username;
    private final EntryField password;
    private final EntryField hostname;
    private final EntryField database;
    private final EntryField port;

    private final FieldListener listener = new FieldListener(this::notifyChange);

    public VerticaConfigEditor(Module module){
        super(new Dimension(300,28));
        username = new EntryField(module);
        password = new EntryField(module);
        hostname = new EntryField(module);
        database = new EntryField(module);
        port = new EntryField(module);

        addField("Username", username);
        addField("Password", password);
        addField("Hostname", hostname);
        addField("Port", port, new Dimension(200,28));
        addField("Database", database);

        listener.listen(username);
        listener.listen(password);
        listener.listen(hostname);
        listener.listen(port);
        listener.listen(database);
    }

    public void setEnabled(boolean enabled){
        super.setEnabled(enabled);
        username.setEnabled(enabled);
        password.setEnabled(enabled);
        hostname.setEnabled(enabled);
        port.setEnabled(enabled);
        database.setEnabled(enabled);
        listener.setEnabled(enabled);
    }

    public void loadConfiguration(Map<String, Object> config) {
        username.setText(StringUtils.defaultString((String)config.get("username"),""));
        password.setText(StringUtils.defaultString((String)config.get("password"),""));
        hostname.setText(StringUtils.defaultString((String)config.get("hostname"),""));
        port.setText(String.valueOf(config.getOrDefault("port","")));
        database.setText(StringUtils.defaultString((String)config.get("database"),""));
    }

    public void saveConfiguration(Map<String, Object> config) {
        config.put("username", username.getText());
        config.put("password", password.getText());
        config.put("hostname", hostname.getText());
        config.put("port", port.getText());
        config.put("database", database.getText());
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
