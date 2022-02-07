package io.datakitchen.ide.editors.neweditors.variation;

import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.JBTable;
import io.datakitchen.ide.editors.DocumentChangeEvent;
import io.datakitchen.ide.editors.DocumentChangeListener;
import io.datakitchen.ide.editors.DocumentEditor;
import io.datakitchen.ide.ui.*;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

public class IngredientEditor extends JPanel implements DocumentEditor {

    private final EventSupport<DocumentChangeListener> listeners = EventSupport.of(DocumentChangeListener.class);

    private final JCheckBox isIngredient = new JCheckBox("Use this variation as ingredient");
    private final JTextField ingredientName = new JTextField();
    private final JTextField shortDescription = new JTextField();
    private final JTextField rollbackIngredient = new JTextField();
    private final InputVariablesModel inputVariablesModel = new InputVariablesModel();
    private final JTable inputVariables = new JBTable();
    private final JEditorPane outputVariables = new JEditorPane();
    private final JEditorPane description = new JEditorPane();
    private final FieldListener listener = new FieldListener(this::saveIngredient);
    private VariationInfo currentVariation;
    private final Action addVariableAction = new SimpleAction("Add", this::addVariable);
    private final Action removeVariableAction = new SimpleAction("Remove", this::removeVariable);
    private final ComboBox<String> inputVariableTypes = new ComboBox<>(new DefaultComboBoxModel<>(new String[]{
            "Alphanumeric",
            "AWS Region",
            "Azure Region",
            "Boolean",
            "Hostname",
            "JSON",
            "Numeric",
            "Password",
            "File Path",
            "Port",
            "Text",
            "Username"
    }));

    public IngredientEditor(){
        setLayout(new BorderLayout());
        add(isIngredient, BorderLayout.NORTH);

        FormPanel panel = new FormPanel();
        panel.addField("Ingredient name", ingredientName);
        panel.addField("Short description",shortDescription);
        panel.addField("Description", wrapInScroll(description), new Dimension(400,60));
        panel.addField("Rollback ingredient",rollbackIngredient);

        JPanel variablesPanel = new JPanel(new BorderLayout());
        variablesPanel.add(wrapInScroll(inputVariables), BorderLayout.CENTER);
        variablesPanel.add(new ButtonsBar(addVariableAction, removeVariableAction),BorderLayout.SOUTH);

        panel.addField("Input variables", variablesPanel, new Dimension(400,100));
        panel.addField("Output variables", wrapInScroll(outputVariables), new Dimension(400,60));

        add(panel, BorderLayout.CENTER);

        inputVariables.setAutoCreateColumnsFromModel(false);

        TableColumn c0 = new TableColumn(0);
        c0.setHeaderValue("Name");
        inputVariables.getColumnModel().addColumn(c0);

        TableColumn c1 = new TableColumn(1);
        c1.setHeaderValue("Display Name");
        inputVariables.getColumnModel().addColumn(c1);

        TableColumn c2 = new TableColumn(2);
        c2.setHeaderValue("Type");
        c2.setCellEditor(new DefaultCellEditor(inputVariableTypes));
        inputVariables.getColumnModel().addColumn(c2);
        inputVariables.setModel(inputVariablesModel);


        listener.listen(isIngredient);
        listener.listen(ingredientName);
        listener.listen(shortDescription);
        listener.listen(description);
        listener.listen(rollbackIngredient);
        listener.listen(inputVariables);
        listener.listen(outputVariables);
        updateState();
    }

    private JComponent wrapInScroll(JComponent c){
        JScrollPane scroll = new JBScrollPane(c, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(new LineBorder(getBackground().brighter()));
        p.add(scroll, BorderLayout.CENTER);
        return p;
    }

    public VariationInfo getCurrentVariation() {
        return currentVariation;
    }

    public void setCurrentVariation(VariationInfo currentVariation) {
        listener.noListen(()->{
            this.currentVariation = currentVariation;

            if (this.currentVariation != null && this.currentVariation.getIngredient() != null) {
                VariationInfo.Ingredient ingredient = currentVariation.getIngredient();
                isIngredient.setSelected(true);
                ingredientName.setText(ingredient.getName());
                shortDescription.setText(ingredient.getShortDescription());
                description.setText(ingredient.getDescription());
                rollbackIngredient.setText(ingredient.getRollbackIngredient());
                inputVariablesModel.setData(ingredient.getRequiredVariables());
                outputVariables.setText(String.join("\n", ingredient.getApplyVariables()));
            } else {
                isIngredient.setSelected(false);
                ingredientName.setText("");
                shortDescription.setText("");
                description.setText("");
                rollbackIngredient.setText("");
                inputVariablesModel.setData(new ArrayList<>());
                outputVariables.setText("");
            }
            updateState();
        });
    }

    private void updateState() {
        isIngredient.setEnabled(currentVariation != null);
        ingredientName.setEnabled(currentVariation != null && isIngredient.isSelected());
        description.setEnabled(currentVariation != null && isIngredient.isSelected());
        shortDescription.setEnabled(currentVariation != null && isIngredient.isSelected());
        rollbackIngredient.setEnabled(currentVariation != null && isIngredient.isSelected());
        inputVariables.setEnabled(currentVariation != null && isIngredient.isSelected());
        outputVariables.setEnabled(currentVariation != null && isIngredient.isSelected());
        addVariableAction.setEnabled(currentVariation != null && isIngredient.isSelected());
        removeVariableAction.setEnabled(currentVariation != null && isIngredient.isSelected() && inputVariables.getRowCount() > 0);
    }
    private void addVariable(ActionEvent event) {
        ((InputVariablesModel)inputVariables.getModel()).addRow();
        updateState();
    }
    private void removeVariable(ActionEvent event) {
        int selectedIndex = inputVariables.getSelectedRow();
        if (selectedIndex != -1) {
            ((InputVariablesModel) inputVariables.getModel()).removeRow(selectedIndex);
            updateState();
        }
    }

    private void saveIngredient() {
        if (currentVariation != null) {
            if (isIngredient.isSelected()) {
                currentVariation.setIngredient(
                    new VariationInfo.Ingredient(
                        ingredientName.getText(),
                        description.getText(),
                        shortDescription.getText(),
                        rollbackIngredient.getText(),
                        inputVariablesModel.getData(),
                        List.of(outputVariables.getText().strip().split("\n"))
                    )
                );
            } else {
                currentVariation.setIngredient(null);
            }
            listeners.getProxy().documentChanged(new DocumentChangeEvent(this));
        }
        updateState();
    }

    @Override
    public void addDocumentChangeListener(DocumentChangeListener listener) {
        listeners.addListener(listener);
    }

    @Override
    public void removeDocumentChangeListener(DocumentChangeListener listener) {
        listeners.removeListener(listener);
    }

    @Override
    public JComponent getEditorComponent() {
        return this;
    }

    private static class InputVariablesModel extends AbstractTableModel {
        private List<VariationInfo.IngredientVariable> variables = new ArrayList<>();

        public InputVariablesModel(){
        }

        public void setData(List<VariationInfo.IngredientVariable> data){
            this.variables = new ArrayList<>(data);
            fireTableDataChanged();
        }

        public List<VariationInfo.IngredientVariable> getData(){
            return variables;
        }

        @Override
        public int getRowCount() {
            return variables.size();
        }

        @Override
        public int getColumnCount() {
            return 3;
        }

        public void addRow(){
            int index = variables.size();
            variables.add(new VariationInfo.IngredientVariable());
            fireTableRowsInserted(index, index);
        }

        public void removeRow(int index){
            variables.remove(index);
            fireTableRowsDeleted(index, index);
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            VariationInfo.IngredientVariable variable = variables.get(rowIndex);

            switch(columnIndex){
                case 0: return variable.getName();
                case 1: return variable.getDisplayName();
                case 2: return variable.getType();
            }
            return null;
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return true;
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            VariationInfo.IngredientVariable variable = variables.get(rowIndex);

            switch(columnIndex){
                case 0:
                    variable.setName((String)aValue);
                    break;
                case 1:
                    variable.setDisplayName((String)aValue);
                    break;
                case 2:
                    variable.setType((String)aValue);
                    break;
            }
            fireTableCellUpdated(rowIndex, columnIndex);
        }
    }
}
