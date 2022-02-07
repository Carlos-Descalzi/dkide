package io.datakitchen.ide.editors.file;

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

public class FTPConfigEditor extends FormPanel implements DocumentEditor {

    private final EventSupport<DocumentChangeListener> eventSupport = EventSupport.of(DocumentChangeListener.class);

    private final EntryField hostname;
    private final EntryField port;
    private final EntryField username;
    private final EntryField password;
    private final JCheckBox passive;

    private final FieldListener listener = new FieldListener(this::notifyChange);

    public FTPConfigEditor(Module module){
        super(new Dimension(300,28));
        hostname = new EntryField(module);
        port = new EntryField(module);
        username = new EntryField(module);
        password = new EntryField(module);
        passive = new JCheckBox();
        addField("Hostname",hostname);
        addField("Port",port);
        addField("Username",username);
        addField("Password",password);
        addField("Passive mode",passive);


        listener.listen(hostname);
        listener.listen(port);
        listener.listen(username);
        listener.listen(password);
    }

    public void setEnabled(boolean enabled){
        super.setEnabled(enabled);
        hostname.setEnabled(enabled);
        port.setEnabled(enabled);
        username.setEnabled(enabled);
        password.setEnabled(enabled);
        passive.setEnabled(enabled);
        listener.setEnabled(enabled);
    }

    public void loadConfiguration(Map<String, Object> config) {
        hostname.setText(StringUtils.defaultString((String)config.get("hostname"),""));
        username.setText(StringUtils.defaultString((String)config.get("username"),""));
        port.setText(String.valueOf(config.getOrDefault("port","")));
        password.setText(StringUtils.defaultString((String)config.get("password"),""));
        passive.setSelected("true".equals(String.valueOf(config.getOrDefault("passive","false"))));
    }

    public void saveConfiguration(Map<String, Object> config) {
        config.put("hostname",hostname.getText());
        config.put("port",port.getText());
        config.put("username",username.getText());
        config.put("password",password.getText());

        if (passive.isSelected()){
            config.put("passive",true);
        }

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
