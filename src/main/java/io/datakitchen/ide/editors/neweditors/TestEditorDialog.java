package io.datakitchen.ide.editors.neweditors;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.openapi.util.IconLoader;
import com.intellij.ui.components.JBScrollPane;
import io.datakitchen.ide.model.*;
import io.datakitchen.ide.ui.FieldWIthOptions;
import io.datakitchen.ide.ui.FormPanel;
import io.datakitchen.ide.util.VariableUtil;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class TestEditorDialog extends DialogWrapper {

    private final FormPanel panel = new FormPanel();
    private final FieldWIthOptions variable = new FieldWIthOptions(this::getVariables);

    private final ComboBox<TestOperator> operator = new ComboBox<>();
    private final ComboBox<MetricConversion> type = new ComboBox<>();
    private final JTextField value = new JTextField();
    private final ComboBox<TestAction> action = new ComboBox<>();
    private final JEditorPane description = new JEditorPane();
    private final Module module;
    private final String nodeName;
    public TestEditorDialog(Module module, String nodeName) {
        super(true);
        this.module = module;
        this.nodeName = nodeName;
        setTitle("Add/Edit Test");

        panel.setDefaultFieldDimension(new Dimension(300,28));
        panel.setLabelDimension(new Dimension(120,28));

        JPanel item = new JPanel(new BorderLayout());
        item.add(variable, BorderLayout.CENTER);

        type.setModel(new DefaultComboBoxModel<>(MetricConversion.values()));
        panel.addField("Test variable", item);
        panel.addField("As", type);
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

        init();
    }

    private List<String> getVariables() {
        try {
            return new ArrayList<>(VariableUtil.getAllDeclaredVariablesUpToNode(module, nodeName, true));
        }catch (Exception ex){
            ex.printStackTrace();
            return List.of();
        }
    }

    public TestEditorDialog(Module module, String nodeName, Test test) {
        this(module, nodeName);
        variable.setText(test.getVariable().getVariableName());
        value.setText(test.getMetricString());
        action.setSelectedItem(test.getTestAction());
        type.setSelectedItem(test.getType());
        description.setText(test.getDescription());

    }

    @Override
    protected @NotNull List<ValidationInfo> doValidateAll() {
        List<ValidationInfo> validations = new ArrayList<>();

        if (StringUtils.isBlank(variable.getText())){
            validations.add(new ValidationInfo("Test variable is required", variable));
        }

        return validations;
    }

    public void updateTest(Test test){
        test.setVariable(new RuntimeVariable(null, variable.getText()));
        test.setMetricString(value.getText());
        test.setTestAction(action.getItem());
        test.setType(type.getItem());
        test.setOperator(operator.getItem());
        test.setDescription(description.getText());
    }


    public Test createTest(){
        return new Test(
            new RuntimeVariable(null, variable.getText()),
            operator.getItem(),
            value.getText(),
            action.getItem(),
            type.getItem()
        );
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        return panel;
    }
}
