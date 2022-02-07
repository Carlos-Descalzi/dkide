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

public class GCSConfigEditor extends FormPanel implements DocumentEditor {

    private final EventSupport<DocumentChangeListener> eventSupport = EventSupport.of(DocumentChangeListener.class);

    private final JRadioButton useKey;
    private final JRadioButton useRole;
    private final JRadioButton useCredentials;

    private final EntryField jsonKey;
    private final EntryField projectId;
    private final EntryField serviceAccount;
    private final EntryField privateKey;
    private final EntryField bucket;

    private final FieldListener listener = new FieldListener(this::notifyChange);

    public GCSConfigEditor(Module module){
        super(new Dimension(300,28));
        bucket = new EntryField(module);
        useKey = new JRadioButton("Key");
        useRole = new JRadioButton("Role");
        useCredentials = new JRadioButton("Credentials");
        jsonKey = new EntryField(module);
        projectId = new EntryField(module);
        serviceAccount = new EntryField(module);
        privateKey = new EntryField(module);

        addField("Bucket",bucket);

        JPanel authMethod = new JPanel(new FlowLayout(FlowLayout.LEFT));
        ButtonGroup group = new ButtonGroup();
        group.add(useKey);
        group.add(useRole);
        group.add(useCredentials);

        authMethod.add(useKey);
        authMethod.add(useRole);
        authMethod.add(useCredentials);

        addField("Auth.method", authMethod);

        addField("Json Key",jsonKey);
        addField("Project ID",projectId);
        addField("Service account",serviceAccount);
        addField("Private key",privateKey);

        listener.listen(bucket);
        listener.listen(useKey);
        listener.listen(useRole);
        listener.listen(useCredentials);
        listener.listen(jsonKey);
        listener.listen(projectId);
        listener.listen(serviceAccount);
        listener.listen(privateKey);
        useKey.setSelected(true);
        updateState();
    }

    private void updateState() {
        jsonKey.setEnabled(useKey.isSelected());
        projectId.setEnabled(useRole.isSelected() || useCredentials.isSelected());
        serviceAccount.setEnabled(useCredentials.isSelected());
        privateKey.setEnabled(useCredentials.isSelected());
    }

    public void setEnabled(boolean enabled){
        super.setEnabled(enabled);
        bucket.setEnabled(enabled);
        useKey.setEnabled(enabled);
        jsonKey.setEnabled(enabled);
        useRole.setEnabled(enabled);
        useCredentials.setEnabled(enabled);
        serviceAccount.setEnabled(enabled);
        privateKey.setEnabled(enabled);
        listener.setEnabled(enabled);
        if (enabled){
            updateState();
        }
    }

    public void loadConfiguration(Map<String, Object> config) {
        bucket.setText(StringUtils.defaultString((String) config.get("bucket"),""));

        String serviceAccount = (String)config.get("service-account");
        if (StringUtils.isNotBlank(serviceAccount)){
            useCredentials.setSelected(true);
            this.serviceAccount.setText(serviceAccount);
        }
        privateKey.setText(StringUtils.defaultString((String)config.get("private-key"),""));

        projectId.setText(StringUtils.defaultString((String)config.get("project-id"),""));

        if (StringUtils.isBlank(serviceAccount) && StringUtils.isNotBlank(projectId.getText())){
            this.useRole.setSelected(true);
        }

        String jsonKey = (String)config.get("service-account-file");
        if (StringUtils.isNotBlank(jsonKey)){
            useKey.setSelected(true);
            this.jsonKey.setText(jsonKey);
        }
    }

    public void saveConfiguration(Map<String, Object> config) {
        config.put("bucket", bucket.getText());

        String projectId = this.projectId.getText();
        if (StringUtils.isNotBlank(projectId)){
            config.put("project-id",projectId);
        }
        if (useKey.isSelected()) {
            config.put("service-account-file", jsonKey.getText());
        } else if (useCredentials.isSelected()){
            config.put("service-account",serviceAccount.getText());
            config.put("private-key",privateKey.getText());
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
