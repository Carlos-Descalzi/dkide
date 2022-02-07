package io.datakitchen.ide.dialogs;

import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.DialogWrapper;
import io.datakitchen.ide.model.DataType;
import io.datakitchen.ide.model.RuntimeVariable;
import io.datakitchen.ide.model.VariableDescription;
import io.datakitchen.ide.ui.FormPanel;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class VariableEditorDialog extends DialogWrapper {

    public enum Type {
        CONNECTOR,
        KEY
    }

    private final DataType dataType;
    private final Type type;
    private final ComboBox<VariableDescription> variableType = new ComboBox<>();
    private final JTextField variableName = new JTextField();

    public VariableEditorDialog(DataType dataType, Type type, RuntimeVariable variable) {
        this(dataType, type);
        variableType.setSelectedItem(variable.getAttribute());
        variableName.setText(variable.getVariableName());
    }

    public VariableEditorDialog(DataType dataType, Type type) {
        super(true);
        setTitle("Add/Edit Variable");
        this.dataType = dataType;
        this.type = type;
        init();
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        FormPanel panel = new FormPanel();

        variableType.setModel(new DefaultComboBoxModel<>(
                type == Type.CONNECTOR
                ? dataType.getConnectorVariables().toArray(VariableDescription[]::new)
                : dataType.getKeyVariables().toArray(VariableDescription[]::new)));

        panel.addField("Attribute", variableType);
        panel.addField("Variable name", variableName);

        return panel;
    }

    public void updateVariable(RuntimeVariable variable){
        variable.setVariableName(variableName.getText());
        variable.setAttribute(variableType.getItem());
    }

    public RuntimeVariable getVariable() {
        return new RuntimeVariable(
            (VariableDescription) variableType.getSelectedItem(),
            variableName.getText()
        );
    }
}
