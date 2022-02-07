package io.datakitchen.ide.editors.file;

import com.intellij.openapi.module.Module;
import io.datakitchen.ide.editors.DocumentChangeEvent;
import io.datakitchen.ide.editors.DocumentChangeListener;
import io.datakitchen.ide.editors.DocumentEditor;
import io.datakitchen.ide.ui.*;
import org.apache.commons.lang.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

public class SFTPConfigEditor extends FormPanel implements DocumentEditor {

    private final EventSupport<DocumentChangeListener> eventSupport = EventSupport.of(DocumentChangeListener.class);

    private final EntryField hostname;
    private final EntryField port;
    private final EntryField username;
    private final EntryField password;
    private final EntryField key;

    private final JRadioButton usePassword;
    private final JRadioButton useKey;

    private final EntryField retryCount;
    private final EntryField retryInterval;

    private final FieldListener listener = new FieldListener(this::notifyChange);

    public SFTPConfigEditor(Module module){
        super(new Dimension(300,28));
        hostname = new EntryField(module);
        port = new EntryField(module);
        username = new EntryField(module);
        password = new EntryField(module);
        key = new EntryField(module);
        retryCount = new EntryField(module);
        retryInterval = new EntryField(module);
        usePassword = new JRadioButton("Password");
        useKey = new JRadioButton("Key file");
        ButtonGroup group = new ButtonGroup();
        group.add(usePassword);
        group.add(useKey);
        addField("Hostname",hostname);
        addField("Port",port);
        addField("Username",username);
        JPanel authOptions = new JPanel(new FlowLayout(FlowLayout.LEFT));
        authOptions.add(usePassword);
        authOptions.add(useKey);
        addField("Auth.method", authOptions);
        addField("Password",password);
        addField("Key file",key);
        addField("Retry count",retryCount);
        addField("Retry interval",retryInterval);


        listener.listen(hostname);
        listener.listen(port);
        listener.listen(username);
        listener.listen(usePassword);
        listener.listen(useKey);
        listener.listen(password);
        listener.listen(key);
        listener.listen(retryCount);
        listener.listen(retryInterval);
        updateState();
    }

    public void setEnabled(boolean enabled){
        super.setEnabled(enabled);
        hostname.setEnabled(enabled);
        port.setEnabled(enabled);
        username.setEnabled(enabled);
        password.setEnabled(enabled && usePassword.isSelected());
        key.setEnabled(enabled && useKey.isSelected());
        retryCount.setEnabled(enabled);
        retryInterval.setEnabled(enabled);
        usePassword.setEnabled(enabled);
        useKey.setEnabled(enabled);

        listener.setEnabled(enabled);
    }

    public void loadConfiguration(Map<String, Object> config) {
        hostname.setText(StringUtils.defaultString((String)config.get("hostname"),""));
        username.setText(StringUtils.defaultString((String)config.get("username"),""));
        port.setText(String.valueOf(config.getOrDefault("port","")));

        String password = StringUtils.defaultString((String)config.get("password"),"");
        if (StringUtils.isNotBlank(password)){
            usePassword.setSelected(true);
        }
        this.password.setText(password);

        String pemFile = StringUtils.defaultString((String)config.get("pem_file"),"");
        if (StringUtils.isNotBlank(pemFile)){
            useKey.setSelected(true);
        }
        this.key.setText(pemFile);
        retryCount.setText(String.valueOf(config.getOrDefault("retry-count","")));
        retryInterval.setText(String.valueOf(config.getOrDefault("retry-interval","")));
    }

    public void saveConfiguration(Map<String, Object> config) {
        config.put("hostname",hostname.getText());
        config.put("port",port.getText());
        config.put("username",username.getText());
        if (usePassword.isSelected()){
            config.put("password",password.getText());
        } else {
            config.put("pem_file",key.getText());
        }
        String retryCount = this.retryCount.getText();
        if (StringUtils.isNotBlank(retryCount)) {
            try {
                config.put("retry-count", Integer.valueOf(retryCount));
            } catch (NumberFormatException ex) {
                config.put("retry-count", retryCount);
            }
        }
        String retryInterval = this.retryInterval.getText();
        if (StringUtils.isNotBlank(retryInterval)) {
            try {
                config.put("retry-interval", Integer.valueOf(retryInterval));
            } catch (NumberFormatException ex) {
                config.put("retry-interval", retryCount);
            }
        }
    }

    private void updateState(){
        password.setEnabled(usePassword.isSelected());
        key.setEnabled(useKey.isEnabled());
    }

    private void notifyChange() {
        updateState();
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
