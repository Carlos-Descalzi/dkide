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

public class S3ConfigEditor extends FormPanel implements DocumentEditor {

    private final EventSupport<DocumentChangeListener> eventSupport = EventSupport.of(DocumentChangeListener.class);

    private final EntryField accessKey;
    private final EntryField secretKey;
    private final EntryField bucket;

    private final FieldListener listener = new FieldListener(this::notifyChange);

    public S3ConfigEditor(Module module){
        super(new Dimension(300,28));
        accessKey = new EntryField(module);
        secretKey = new EntryField(module);
        bucket = new EntryField(module);

        addField("Access key",accessKey);
        addField("Secret key",secretKey);
        addField("Bucket",bucket);


        listener.listen(accessKey);
        listener.listen(secretKey);
        listener.listen(bucket);
    }

    public void setEnabled(boolean enabled){
        super.setEnabled(enabled);
        accessKey.setEnabled(enabled);
        secretKey.setEnabled(enabled);
        bucket.setEnabled(enabled);
        listener.setEnabled(enabled);
    }

    public void loadConfiguration(Map<String, Object> config) {
        accessKey.setText(StringUtils.defaultString((String)config.get("access-key"),""));
        secretKey.setText(StringUtils.defaultString((String)config.get("secret-key"),""));
        bucket.setText(StringUtils.defaultString((String)config.get("bucket"),""));
    }

    public void saveConfiguration(Map<String, Object> config) {
        config.put("access-key", accessKey.getText());
        config.put("secret-key", secretKey.getText());
        config.put("bucket", bucket.getText());
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
