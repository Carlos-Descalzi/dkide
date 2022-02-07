package io.datakitchen.ide.editors.variables;

import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.EditorTextField;
import com.intellij.ui.components.JBScrollPane;
import io.datakitchen.ide.ui.FormLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

public class VariableEditor extends JPanel {

    private final JTextField variableName = new JTextField();
    private final ComboBox<Variable.Type> variableType = new ComboBox<>(new DefaultComboBoxModel<>(Variable.Type.values()));
//    private JTextArea textPane = new JBTextArea();
    private final EditorTextField textPane;

    private Variable currentVariable;

    public VariableEditor(){
        setLayout(new FormLayout(5,5));
        textPane = new EditorTextField();
        textPane.setFont(new Font("Monospaced",Font.PLAIN,14));

        JLabel l = new JLabel("Name");
        l.setPreferredSize(new Dimension(100,28));
        l.setLabelFor(variableName);
        variableName.setPreferredSize(new Dimension(200,28));
        add(l);
        add(variableName);

        l = new JLabel("Type");
        variableType.setPreferredSize(new Dimension(200,28));
        l.setLabelFor(variableType);
        add(l);
        add(variableType);

        l = new JLabel("Value");

        JScrollPane valueScroll = new JBScrollPane(textPane, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        valueScroll.setPreferredSize(new Dimension(800,600));
        l.setLabelFor(valueScroll);
        add(l);
        add(valueScroll);

        FocusListener listener = new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                updateVariable();
            }
        };

        variableName.addFocusListener(listener);
        textPane.addFocusListener(listener);
    }

    public Variable getCurrentVariable() {
        return currentVariable;
    }

    public void setCurrentVariable(Variable currentVariable) {
        if (this.currentVariable != null){
            updateVariable();
        }
        this.currentVariable = currentVariable;
        updateView();
    }

    private void updateVariable(){
        currentVariable.setName(variableName.getText());
        currentVariable.setType(variableType.getItem());
        currentVariable.setValue(textPane.getText());
    }

    private void updateView(){
        if (currentVariable != null){
            variableName.setText(currentVariable.getName());
            variableType.setSelectedItem(currentVariable.getType());
            textPane.setText(null);
            textPane.setText(currentVariable.getValue());
        } else {
            variableName.setText("");
            variableType.setSelectedIndex(0);
            textPane.setText("");
        }
    }

}
