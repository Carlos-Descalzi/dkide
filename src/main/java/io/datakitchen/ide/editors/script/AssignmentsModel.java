package io.datakitchen.ide.editors.script;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

public class AssignmentsModel extends AbstractTableModel {

    private static final String[] COLUMN_NAMES = {"Variable", "File name"};

    private List<VariableAssignment> items = new ArrayList<>();

    @Override
    public int getRowCount() {
        return items.size();
    }

    @Override
    public int getColumnCount() {
        return 2;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        VariableAssignment item = items.get(rowIndex);
        switch(columnIndex){
            case 0:
                return item.getVariable();
            case 1:
                return item.getFilename();
        }
        return null;
    }

    @Override
    public String getColumnName(int column) {
        return COLUMN_NAMES[column];
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return true;
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        VariableAssignment item = items.get(rowIndex);
        switch (columnIndex){
            case 0:
                item.setVariable((String)aValue);
                break;
            case 1:
                item.setFilename((String)aValue);
                break;
        }
        fireTableCellUpdated(rowIndex, columnIndex);
    }

    public List<VariableAssignment> getVariableAssignments() {
        return items;
    }

    public void setVariableAssignments(List<VariableAssignment> variableAssignments){
        this.items = variableAssignments;
        fireTableDataChanged();
    }

    public void remove(int index) {
        this.items.remove(index);
        fireTableRowsDeleted(index,index);
    }

    public void add(VariableAssignment variableAssignment) {
        int index = this.items.size();
        this.items.add(variableAssignment);
        fireTableRowsInserted(index,index);
    }
}
