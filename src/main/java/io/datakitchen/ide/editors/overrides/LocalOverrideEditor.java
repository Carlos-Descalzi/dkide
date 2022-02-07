package io.datakitchen.ide.editors.overrides;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.util.Disposer;
import io.datakitchen.ide.model.ConnectorNature;
import io.datakitchen.ide.model.ConnectorType;
import io.datakitchen.ide.ui.FormPanel;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class LocalOverrideEditor extends FormPanel implements ChangeListener, Disposable {
    private final JTextField name = new JTextField();
    private final ComboBox<ValueType> type = new ComboBox<>();
    private ValueType currentType = null;
    private LocalOverride currentValue = null;
    private final JPanel valueHolder = new JPanel(new BorderLayout());

    private final ValueType jsonEditor;
    private final List<ValueType> valueTypes = new ArrayList<>();
    private final ActionListener listener = e ->changeEditor();

    public void clear() {
        name.setText("");
        currentValue = null;
        currentType = null;
        valueHolder.removeAll();
        valueHolder.revalidate();
    }

    public LocalOverrideEditor(Module module){
        addField("Override name", name);
        addField("Type", type);
        addField("Value", valueHolder);
        JsonEditor editor = new JsonEditor(module);
        Disposer.register(this, editor);
        jsonEditor  = new ValueType("json", "JSON", editor);
        Disposer.register(this, (JsonEditor)jsonEditor.editor);
        valueTypes.add(jsonEditor);

        for (ConnectorType type: ConnectorType.values()){
            try {
                if (type.getNature() != ConnectorNature.DICT) { // TODO
                    valueTypes.add(
                            new ValueType(
                                    type.getSchema(),
                                    type.getDescription(),
                                    new StructEditor(module, type.getSchema())
                            )
                    );
                }
            }catch(Exception ignored){}
        }

        valueHolder.setPreferredSize(new Dimension(600,500));
        type.setModel(new DefaultComboBoxModel<>(valueTypes.toArray(ValueType[]::new)));
        type.addActionListener(listener);
        updateEditor();
    }


    private void changeEditor() {
        Object currentValue;
        try {
            currentValue = currentType.editor.getValue();
        }catch (Exception ex){
            ex.printStackTrace();
            currentValue = this.currentValue.getContent();
        }
        updateEditor();
        currentType.editor.setValue(currentValue);
        this.currentValue.setTypeName(currentType.typeName);
    }

    private void updateEditor(){
        ValueType value = type.getItem();
        if (value == null){
            value = jsonEditor;
        }
        if (currentType != null){
            currentType.editor.removeChangeListener(this);
        }
        valueHolder.removeAll();
        valueHolder.add(value.editor.getComponent());
        valueHolder.revalidate();
        revalidate();
        repaint();
        currentType = value;
        currentType.editor.addChangeListener(this);
    }

    @Override
    public void dispose() {

    }

    public void updateCurrent(){
        if (currentValue != null){
            try {
                currentValue.setContent(currentType.editor.getValue());
            }catch (Exception ex){
                ex.printStackTrace();
            }
        }
    }

    public void setValue(LocalOverride value) {
        type.removeActionListener(listener);

        if (currentValue != null){
            try {
                currentValue.setContent(currentType.editor.getValue());
            }catch (Exception ex){
                ex.printStackTrace();
            }
        }

        name.setText(value.getName());
        type.setSelectedItem(findTypeByTypeName(value.getTypeName()));
        updateEditor();
        currentValue = value;
        currentType.editor.setValue(value.getContent());

        type.addActionListener(listener);
    }

    private ValueType findTypeByTypeName(String typeName){
        for (ValueType valueType: valueTypes){
            if (valueType.getTypeName().equals(typeName)){
                return valueType;
            }
        }
        return jsonEditor;
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        if (currentValue != null && currentType != null){
            try {
                currentValue.setContent(currentType.editor.getValue());
            }catch(Exception ex){
                ex.printStackTrace();
            }
        }
    }


    private static class ValueType {
        private final String typeName;
        private final String description;
        private final ValueEditor editor;

        public ValueType(String typeName, String description, ValueEditor editor){
            this.typeName = typeName;
            this.description = description;
            this.editor = editor;
        }

        public String getTypeName(){
            return typeName;
        }

        public String toString(){
            return description;
        }
    }

}
