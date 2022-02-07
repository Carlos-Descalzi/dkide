package io.datakitchen.ide.editors.overrides;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.module.Module;
import io.datakitchen.ide.Constants;
import io.datakitchen.ide.editors.types.JsonSchemaForm;
import io.datakitchen.ide.util.JsonUtil;

import javax.swing.*;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class StructEditor extends JsonSchemaForm implements ValueEditor, Disposable {

    public StructEditor(Module module, String type){
        super(module);
        Map<String, Object> schema;
        try {
            schema = JsonUtil.read(Objects.requireNonNull(
                    getClass().getResource("/schemas/" + type.replace(Constants.SCHEMA_PREFIX, "") + ".json")
            ));
        }catch (Exception ex){
            schema = new LinkedHashMap<>();
            System.err.println("Error loading schema definition for "+type);
            ex.printStackTrace();
        }
        setSchema(type, schema);
//        add(value, BorderLayout.CENTER);
    }

    @Override
    public Object getValue() {
        return getJson();
    }

    @Override
    public void setValue(Object value) {
        setJson((Map<String, Object>) value);
    }

    @Override
    public JComponent getComponent() {
        return this;
    }

    @Override
    public void dispose() {

    }
}
