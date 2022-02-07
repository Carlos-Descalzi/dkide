package io.datakitchen.ide.dialogs;

import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.DialogWrapper;
import io.datakitchen.ide.model.Condition;
import io.datakitchen.ide.model.MetricConversion;
import io.datakitchen.ide.ui.FormPanel;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class ConditionEditorDialog extends DialogWrapper {

    private JTextField name;
    private JTextField variableName;
    private JComboBox<MetricConversion> conversion;

    public ConditionEditorDialog() {
        super(true);
        init();
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {

        FormPanel topPanel = new FormPanel();
        name = new JTextField();
        topPanel.addField("Name",name);
        variableName = new JTextField();
        topPanel.addField("Variable",variableName);
        conversion = new ComboBox<>(new DefaultComboBoxModel<>(MetricConversion.values()));
        topPanel.addField("Evaluate as",conversion);

        return topPanel;
    }

    public void load(Condition condition){
        condition.setConditionName(name.getText());
        variableName.setText(condition.getVariable());
        conversion.setSelectedItem(condition.getConversion());
    }

    public void save(Condition condition){
        condition.setConditionName(name.getText());
        condition.setVariable(variableName.getText());
        condition.setConversion((MetricConversion) conversion.getSelectedItem());
        condition.setType(Condition.Type.SELECT);
    }
}
