package io.datakitchen.ide.editors.ingredient;

import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.JBTable;
import com.intellij.util.ui.JBUI;
import io.datakitchen.ide.editors.DocumentChangeEvent;
import io.datakitchen.ide.editors.DocumentChangeListener;
import io.datakitchen.ide.ui.EventSupport;
import io.datakitchen.ide.ui.FieldListener;
import io.datakitchen.ide.ui.FormPanel;
import io.datakitchen.ide.ui.SimpleAction;
import io.datakitchen.ide.util.ObjectUtil;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class IngredientEditor extends FormPanel {
    private final EventSupport<DocumentChangeListener> eventSupport = EventSupport.of(DocumentChangeListener.class);

    private final JTextField shortDescription = new JTextField();
    private final JTextArea description = new JTextArea();
    private final ComboBox<String> rollbackIngredient = new ComboBox<>();
    private final JTable inputVariables = new JBTable();
    private final JTable outputVariables = new JBTable();
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

    private final Action addInputVariableAction = new SimpleAction("Add", this::addInputVariable);
    private final Action removeInputVariableAction = new SimpleAction("Remove", this::removeInputVariable);

    private final Action addOutputVariableAction = new SimpleAction("Add", this::addOutputVariable);
    private final Action removeOutputVariableAction = new SimpleAction("Remove", this::removeOutputVariable);

    private final FieldListener listener = new FieldListener(this::notifyChanges);

    public IngredientEditor(){
        setBorder(JBUI.Borders.empty(10));
        addField("Short Description",shortDescription,new Dimension(300,28));
        addField("Ingredient description", new JBScrollPane(description, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED),
                new Dimension(400,200));

        addField("Rollback ingredient",rollbackIngredient);
        JPanel inputVariablesPanel = new JPanel(new BorderLayout());
        inputVariablesPanel.add(new JBScrollPane(inputVariables, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER),BorderLayout.CENTER);
        JPanel inputVariablesButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        inputVariablesButtons.add(new JButton(addInputVariableAction));
        inputVariablesButtons.add(new JButton(removeInputVariableAction));
        inputVariablesPanel.add(inputVariablesButtons,BorderLayout.SOUTH);

        addField("Input variables", inputVariablesPanel, new Dimension(400,200));

        JPanel outputVariablesPanel = new JPanel(new BorderLayout());
        outputVariablesPanel.add(new JBScrollPane(outputVariables, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED), BorderLayout.CENTER);
        JPanel outputVariablesButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        outputVariablesButtons.add(new JButton(addOutputVariableAction));
        outputVariablesButtons.add(new JButton(removeOutputVariableAction));
        outputVariablesPanel.add(outputVariablesButtons, BorderLayout.SOUTH);

        addField("Output variables", outputVariablesPanel, new Dimension(400,200));

        inputVariables.setAutoCreateColumnsFromModel(false);

        //{"Name","Display name","Type"}
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

        inputVariables.setModel(new InputVariableTableModel());
        outputVariables.setModel(new OutputVariablesTableModel());

        listener.listen(inputVariables);
        listener.listen(outputVariables);
//        listener.listen(name);
        listener.listen(shortDescription);
        listener.listen(rollbackIngredient);
        listener.listen(description);

        updateActions();
    }

    public void addDocumentChangeListener(DocumentChangeListener listener) {
        this.eventSupport.addListener(listener);
    }

    public void removeDocumentChangeListener(DocumentChangeListener listener) {
        this.eventSupport.removeListener(listener);
    }

    private void notifyChanges() {
        updateActions();
        eventSupport.getProxy().documentChanged(new DocumentChangeEvent(this));
    }

    private void updateActions(){
        removeInputVariableAction.setEnabled(inputVariables.getSelectedRow() != -1);
        removeOutputVariableAction.setEnabled(outputVariables.getSelectedRow() != -1);
    }

    private void addInputVariable(ActionEvent event) {
        ((InputVariableTableModel)inputVariables.getModel()).addRow();
        updateActions();
    }

    private void removeInputVariable(ActionEvent event) {
        int index = inputVariables.getSelectedRow();
        if (index != -1){
            ((InputVariableTableModel)inputVariables.getModel()).removeRow(index);
        }
        updateActions();
    }

    private void addOutputVariable(ActionEvent event) {
        ((OutputVariablesTableModel)outputVariables.getModel()).addRow();
        updateActions();
    }

    private void removeOutputVariable(ActionEvent event) {
        int index = outputVariables.getSelectedRow();
        if (index != -1){
            ((OutputVariablesTableModel)outputVariables.getModel()).removeRow(index);
        }
        updateActions();
    }

    public void loadItem(IngredientItem currentItem) {
        listener.setEnabled(false);

        if (currentItem != null) {
//            name.setText((String) currentItem.getContent().getOrDefault("ingredient-name",""));
            shortDescription.setText((String) currentItem.getContent().getOrDefault("short-description",""));
            description.setText((String) currentItem.getContent().getOrDefault("description",""));

            List<Map<String, Object>> inputVariables = ObjectUtil.cast(currentItem.getContent().get("required-recipe-variables"));

            if (inputVariables == null) {
                inputVariables = new ArrayList<>();
            }

            this.inputVariables.setModel(new InputVariableTableModel(inputVariables));

            List<String> outputVariables = ObjectUtil.cast(currentItem.getContent().get("apply-runtime-recipe-variables"));

            if (outputVariables == null) {
                outputVariables = new ArrayList<>();
            }
            this.outputVariables.setModel(new OutputVariablesTableModel(outputVariables));
            rollbackIngredient.setSelectedItem(currentItem.getContent().get("rollback-ingredient"));
            setEnabled(true);
        } else {
//            name.setText("");
            shortDescription.setText("");
            description.setText("");
            inputVariables.setModel(new InputVariableTableModel());
            outputVariables.setModel(new OutputVariablesTableModel());
            rollbackIngredient.setSelectedItem(null);
            setEnabled(false);
        }
        listener.setEnabled(true);
    }

    public void setEnabled(boolean enabled){
        shortDescription.setEnabled(enabled);
        description.setEnabled(enabled);
        rollbackIngredient.setEnabled(enabled);
        inputVariables.setEnabled(enabled);
        outputVariables.setEnabled(enabled);
    }

    public void saveIngredient(IngredientItem currentItem) {
        currentItem.getContent().put("short-description",shortDescription.getText());
        currentItem.getContent().put("description",description.getText());
        List<Map<String, Object>> inputVariables = ((InputVariableTableModel)this.inputVariables.getModel()).items;
        currentItem.getContent().put("required-recipe-variables",inputVariables);
        List<String> outputVariables = ((OutputVariablesTableModel)this.outputVariables.getModel()).items;
        currentItem.getContent().put("apply-runtime-recipe-variables",outputVariables);

        String rollbackIngredient = (String)this.rollbackIngredient.getSelectedItem();

        if (rollbackIngredient != null){
            currentItem.getContent().put("rollback-ingredient",rollbackIngredient);
        }

    }

    private static class InputVariableTableModel extends AbstractTableModel {

        private static final String[] COLUMN_NAMES = {"Name","Display name","Type"};

        private final List<Map<String, Object>> items;

        public InputVariableTableModel(){
            this(new ArrayList<>());
        }

        public InputVariableTableModel(List<Map<String,Object>> items){
            this.items = items;
        }

        @Override
        public int getRowCount() {
            return items.size();
        }

        @Override
        public int getColumnCount() {
            return 3;
        }

        @Override
        public String getColumnName(int column) {
            return COLUMN_NAMES[column];
        }

        public void addRow(){
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("name","");
            item.put("display-name","");
            item.put("type","");
            int index = items.size();
            items.add(item);
            fireTableRowsInserted(index, index);
        }

        public void removeRow(int index){
            items.remove(index);
            fireTableRowsDeleted(index, index);
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            Map<String, Object> item = items.get(rowIndex);
            switch (columnIndex){
                case 0:
                    return item.get("name");
                case 1:
                    return item.get("display-name");
                case 2:
                    return item.get("type");
            }
            return null;
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return true;
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            Map<String, Object> item = items.get(rowIndex);
            switch (columnIndex){
                case 0:
                    item.put("name", aValue);
                    break;
                case 1:
                    item.put("display-name",aValue);
                    break;
                case 2:
                    item.put("type",aValue);
                    break;
            }
            fireTableCellUpdated(rowIndex, columnIndex);
        }
    }

    private static class OutputVariablesTableModel extends AbstractTableModel {
        private final List<String> items;

        public OutputVariablesTableModel(){
            this(new ArrayList<>());
        }

        public OutputVariablesTableModel(List<String> items){
            this.items = items;
        }

        public void addRow(){
            int index = items.size();
            items.add("");
            fireTableRowsInserted(index,index);
        }

        public void removeRow(int index){
            items.remove(index);
            fireTableRowsDeleted(index,index);
        }

        @Override
        public int getRowCount() {
            return items.size();
        }

        @Override
        public int getColumnCount() {
            return 1;
        }

        @Override
        public String getColumnName(int column) {
            return "Name";
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return true;
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            items.remove(rowIndex);
            items.add(rowIndex, (String)aValue);
            fireTableCellUpdated(rowIndex,columnIndex);
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            return items.get(rowIndex);
        }
    }

    public void setIngredientList(List<String> ingredientList){
        listener.setEnabled(false);
        List<String> items = new ArrayList<>();
        items.add(null);
        items.addAll(ingredientList);
        Object selectedItem = rollbackIngredient.getSelectedItem();
        rollbackIngredient.setModel(new DefaultComboBoxModel<>(items.toArray(String[]::new)));
        rollbackIngredient.setSelectedItem(selectedItem);
        listener.setEnabled(true);
    }
}
