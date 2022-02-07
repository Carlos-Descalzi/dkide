package io.datakitchen.ide.editors.types;

import com.intellij.openapi.module.Module;
import io.datakitchen.ide.ui.EntryField;
import io.datakitchen.ide.ui.EventSupport;
import io.datakitchen.ide.ui.FormPanel;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.LinkedHashMap;
import java.util.Map;

public class JsonSchemaForm extends FormPanel {
    private final EventSupport<ChangeListener> listeners = EventSupport.of(ChangeListener.class);

    private String schemaUrl;
    private Map<String, Object> schemaDefinition;
    private final Map<String, EntryField> fields = new LinkedHashMap<>();
    private Map<String, Object> json;
    private final Module module;

    public JsonSchemaForm(Module module){
        super(new Dimension(350,28));
        this.module = module;
    }

    public void setSchema(String schemaUrl, Map<String, Object> schemaDefinition) {
        this.schemaUrl = schemaUrl;
        this.schemaDefinition = schemaDefinition;

        Map<String, Object> properties = (Map<String, Object>) schemaDefinition.get("properties");

        for (Map.Entry<String, Object> entry:properties.entrySet()){

            Map<String, Object> fieldInfo = (Map<String, Object>)entry.getValue();
            EntryField field = new EntryField(module);
            field.addFocusListener(new FocusAdapter() {
                @Override
                public void focusLost(FocusEvent e) {
                    checkChange(entry.getKey(), (EntryField)e.getSource());
                }
            });
            String description = (String)fieldInfo.getOrDefault("description", entry.getKey());

            addField(description, field);

            fields.put(entry.getKey(), field);
        }
    }

    private void checkChange(String key, EntryField source) {
        if (json != null || !source.getText().equals(String.valueOf(json.get(key)))){
            listeners.getProxy().stateChanged(new ChangeEvent(this));
        }
    }

    public Map<String, Object> getJson(){
        Map<String, Object> json = new LinkedHashMap<>();
        json.put("_schema", schemaUrl);

        for (Map.Entry<String, EntryField> entry: fields.entrySet()){
            json.put(entry.getKey(), entry.getValue().getText());
        }

        return json;
    }

    public void setJson(Map<String, Object> json){
        this.json = json;
        for (EntryField field:fields.values()){
            field.setText("");
        }
        if (json != null) {
            for (Map.Entry<String, Object> entry : json.entrySet()) {
                EntryField field = fields.get(entry.getKey());
                if (field != null) {
                    field.setText(entry.getValue() != null ? String.valueOf(entry.getValue()) : null);
                }
            }
        }
    }


    public void addChangeListener(ChangeListener listener) {
        listeners.addListener(listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        listeners.removeListener(listener);
    }

}
