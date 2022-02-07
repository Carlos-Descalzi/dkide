package io.datakitchen.ide.editors.neweditors;

import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.openapi.util.IconLoader;
import com.intellij.ui.components.JBScrollPane;
import io.datakitchen.ide.model.*;
import io.datakitchen.ide.ui.FormPanel;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

public class FileTestEditorDialog extends DialogWrapper {

    private final Key key;
    private final FormPanel panel = new FormPanel();
    private final ComboBox<VariableDescription> variable = new ComboBox<>();
    private final ComboBox<MetricConversion> conversion = new ComboBox<>();
    private final ComboBox<TestOperator> operator = new ComboBox<>();
    private final JTextField value = new JTextField();
    private final ComboBox<TestAction> action = new ComboBox<>();
    private final JEditorPane description = new JEditorPane();
    private boolean update = false;

    public FileTestEditorDialog(DataType dsType, FileTest fileTest) {
        this(dsType, fileTest.getKey());
        update = true;
        variable.setEnabled(false);
        VariableDescription variableAttribute = fileTest.getVariable().getAttribute();
        variable.setSelectedItem(variableAttribute);
        conversion.setSelectedItem(variableAttribute.getType() != null ? variableAttribute.getType() : fileTest.getType());
        operator.setSelectedItem(fileTest.getOperator());
        value.setText(fileTest.getMetricString());
        action.setSelectedItem(fileTest.getTestAction());
        description.setText(fileTest.getDescription());
    }
    @Override
    protected @NotNull List<ValidationInfo> doValidateAll() {
        List<ValidationInfo> validations = new ArrayList<>();

        if (StringUtils.isBlank(value.getText())){
            validations.add(new ValidationInfo("Value is required", value));
        }
        if (conversion.getItem() == null){
            validations.add(new ValidationInfo("Type is required", conversion));
        }

        return validations;
    }
    public FileTestEditorDialog(DataType dsType, Key key) {
        super(true);
        setTitle("Add/Edit File Test");
        this.key = key;

        panel.setDefaultFieldDimension(new Dimension(300,28));
        panel.setLabelDimension(new Dimension(80,28));

        variable.setModel(new DefaultComboBoxModel<>(dsType.getKeyVariables().toArray(VariableDescription[]::new)));
        variable.addActionListener(this::updateConversion);
        JPanel item = new JPanel(new BorderLayout());
        item.add(variable, BorderLayout.CENTER);
        if (key != null) {
            item.add(new JLabel(" of " + key.getDescription()), BorderLayout.EAST);
        }
        panel.addField("Test", item);
        conversion.setModel(new DefaultComboBoxModel<>(MetricConversion.values()));
        panel.addField("As", conversion);
        
        operator.setModel(new DefaultComboBoxModel<>(TestOperator.values()));
        

        JPanel comparator = new JPanel(new BorderLayout());
        comparator.add(operator, BorderLayout.WEST);
        comparator.add(value, BorderLayout.CENTER);
        
        panel.addField("",comparator);

        panel.addField("When fail",action);
        panel.addField("Description",
            new JBScrollPane(description, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED),
            new Dimension(400,80)
        );
        action.setModel(new DefaultComboBoxModel<>(TestAction.values()));
        action.setRenderer(new DefaultListCellRenderer(){
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

                if (value != null){
                    TestAction action = (TestAction)value;
                    label.setIcon(IconLoader.getIcon("/"+action.getIconName(),getClass()));
                } else {
                    label.setIcon(null);
                }

                return label;
            }
        });
        updateConversion(null);
        init();
    }

    private void updateConversion(ActionEvent event) {
        VariableDescription variableDesc = (VariableDescription) variable.getSelectedItem();
        MetricConversion type = variableDesc.getType();
        if (type != null){
            conversion.setSelectedItem(type);
            conversion.setEnabled(false);
        } else {
            conversion.setEnabled(true);
        }
    }

    public void updateTest(FileTest fileTest){
        fileTest.setOperator(operator.getItem());
        fileTest.setMetricString(value.getText());
        if (!update) {
            fileTest.setVariable(new RuntimeVariable(variable.getItem(), null));
        }
        fileTest.setTestAction(action.getItem());
        fileTest.setType(conversion.isEnabled() ? conversion.getItem() : null);
        fileTest.setDescription(description.getText());
    }

    public FileTest createTest(){

        return new FileTest(key,
            new RuntimeVariable(variable.getItem(), null),
            operator.getItem(),
            value.getText(),
            action.getItem(),
            conversion.isEnabled() ? conversion.getItem() : null
        );
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        return panel;
    }
}
