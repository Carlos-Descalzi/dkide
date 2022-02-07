package io.datakitchen.ide.editors.variables;

import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBScrollPane;
import io.datakitchen.ide.editors.DocumentChangeListener;
import io.datakitchen.ide.ui.EventSupport;
import io.datakitchen.ide.ui.SimpleAction;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.LinkedHashMap;
import java.util.Map;

public class VariableListEditor extends JPanel {

    private final DefaultListModel model = new DefaultListModel();
    private final JList variableList = new JBList(model);
    private final VariableEditor contentPane = new VariableEditor();
    private final Action addVariableAction = new SimpleAction("Add", this::addVariable);
    private final Action removeVariableAction = new SimpleAction("Remove", this::removeVariable);

    private Variable currentVariable = null;
    private final EventSupport<DocumentChangeListener> eventSupport = EventSupport.of(DocumentChangeListener.class);

    public void addDocumentChangeListener(DocumentChangeListener listener){
        this.eventSupport.addListener(listener);
    }
    public void removeDocumentChangeListener(DocumentChangeListener listener){
        this.eventSupport.removeListener(listener);
    }

    public VariableListEditor(){
        setLayout(new BorderLayout());

        JPanel leftPanel = new JPanel(new BorderLayout());
        add(leftPanel, BorderLayout.WEST);
        leftPanel.add(new JBScrollPane(variableList,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED),BorderLayout.CENTER);
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(new JButton(addVariableAction));
        buttons.add(new JButton(removeVariableAction));
        leftPanel.add(buttons,BorderLayout.SOUTH);
        add(contentPane, BorderLayout.CENTER);
        contentPane.setBorder(new EmptyBorder(10,10,10,10));

        variableList.addListSelectionListener(this::updateVariableSelection);

        updateActions();
    }

    public void setVariables(Map<String, Object> overrides) {
        model.removeAllElements();
        for (Map.Entry<String, Object> entry:overrides.entrySet()){
            model.addElement(new Variable(entry.getKey(), entry.getValue()));
        }
    }

    public Map<String, Object> getVariables() {
        Map<String, Object> variables = new LinkedHashMap<>();

        for (int i=0;i<model.getSize();i++){
            Variable variable = (Variable)model.getElementAt(i);

            variables.put(variable.getName(),variable.getJsonValue());
        }

        return variables;
    }
    private void updateVariableSelection(ListSelectionEvent e){
        int index = variableList.getSelectedIndex();
        if (index != -1){
            currentVariable = (Variable) model.getElementAt(index);
        } else {
            currentVariable = null;
        }
        contentPane.setCurrentVariable(currentVariable);
        updateActions();
    }

    private void updateActions(){
        int selected = variableList.getSelectedIndex();
        removeVariableAction.setEnabled(selected != -1);
    }

    private void removeVariable(ActionEvent actionEvent) {
        int index = variableList.getSelectedIndex();
        if (index != -1){
            model.removeElementAt(index);
        }
        updateActions();
    }

    private void addVariable(ActionEvent actionEvent) {
        model.addElement(new Variable());
        updateActions();
    }

}
