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

public class AzureBlobConfigEditor extends FormPanel implements DocumentEditor {

    private final EventSupport<DocumentChangeListener> eventSupport = EventSupport.of(DocumentChangeListener.class);

    private final EntryField connectionString;
    private final EntryField container;

    private final FieldListener listener = new FieldListener(this::notifyChange);

    public AzureBlobConfigEditor(Module module){
        super(new Dimension(300,28));
        connectionString = new EntryField(module);
        container = new EntryField(module);

        addField("Connection String",connectionString);
        addField("Container",container);


        listener.listen(connectionString);
        listener.listen(container);
    }

    public void setEnabled(boolean enabled){
        super.setEnabled(enabled);
        connectionString.setEnabled(enabled);
        container.setEnabled(enabled);
        listener.setEnabled(enabled);
    }

    public void loadConfiguration(Map<String, Object> config) {
        connectionString.setText(StringUtils.defaultString((String)config.get("connection_string"),""));
        container.setText(StringUtils.defaultString((String)config.get("container"),""));
    }

    public void saveConfiguration(Map<String, Object> config) {
        config.put("connection_string", connectionString.getText());
        config.put("container", container.getText());
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
