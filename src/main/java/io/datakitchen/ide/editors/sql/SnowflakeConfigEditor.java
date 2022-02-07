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

public class SnowflakeConfigEditor extends FormPanel implements DocumentEditor {

    private final EventSupport<DocumentChangeListener> eventSupport = EventSupport.of(DocumentChangeListener.class);

    private final EntryField username;
    private final EntryField password;
    private final EntryField account;
    private final EntryField warehouse;

    private final FieldListener listener = new FieldListener(this::notifyChange);

    public SnowflakeConfigEditor(Module module){
        super(new Dimension(300,28));
        username = new EntryField(module);
        password = new EntryField(module);
        account = new EntryField(module);
        warehouse = new EntryField(module);

        addField("Account", account);
        addField("Username", username);
        addField("Password", password);
        addField("Warehouse", warehouse);

        listener.listen(username);
        listener.listen(password);
        listener.listen(account);
        listener.listen(warehouse);
    }

    public void setEnabled(boolean enabled){
        super.setEnabled(enabled);
        username.setEnabled(enabled);
        password.setEnabled(enabled);
        account.setEnabled(enabled);
        warehouse.setEnabled(enabled);
        listener.setEnabled(enabled);
    }

    public void loadConfiguration(Map<String, Object> config) {
        username.setText(StringUtils.defaultString((String)config.get("username"),""));
        password.setText(StringUtils.defaultString((String)config.get("password"),""));
        account.setText(StringUtils.defaultString((String)config.get("account"),""));
        warehouse.setText(StringUtils.defaultString((String)config.get("warehouse"),""));
    }

    public void saveConfiguration(Map<String, Object> config) {
        config.put("username", username.getText());
        config.put("password", password.getText());
        config.put("account", account.getText());
        config.put("warehouse", warehouse.getText());
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
