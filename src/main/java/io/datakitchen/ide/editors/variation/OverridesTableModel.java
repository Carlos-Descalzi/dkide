package io.datakitchen.ide.editors.variation;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class OverridesTableModel extends AbstractTableModel {
    private final List<OverrideItem> items;
    private static final Class[] COLUMN_TYPES = new Class[]{Boolean.TYPE, String.class};
    private static final String[] COLUMN_NAMES = {"Select", "Name"};

    public OverridesTableModel() {
        this.items = new ArrayList<>();
    }

    public OverridesTableModel(List<String> overrideNames) {
        this.items = overrideNames.stream()
                .map(OverrideItem::new).collect(Collectors.toList());
    }

    @Override
    public int getRowCount() {
        return items.size();
    }

    public void setSelected(List<String> names) {
        Set<String> selected = Set.copyOf(names);

        for (OverrideItem item : items) {
            item.setSelected(selected.contains(item.getName()));
        }
        fireTableDataChanged();
    }

    public List<String> getSelected() {
        List<String> selected = new ArrayList<>();

        for (OverrideItem item : items) {
            if (item.isSelected()) {
                selected.add(item.getName());
            }
        }

        return selected;
    }

    @Override
    public int getColumnCount() {
        return 2;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        OverrideItem item = items.get(rowIndex);
        switch (columnIndex) {
            case 0:
                return item.isSelected();
            case 1:
                return item.getName();
        }
        return null;
    }

    @Override
    public String getColumnName(int column) {
        return COLUMN_NAMES[column];
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return COLUMN_TYPES[columnIndex];
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex == 0;
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        OverrideItem item = items.get(rowIndex);
        if (columnIndex == 0) {
            item.setSelected((Boolean) aValue);
            fireTableCellUpdated(rowIndex, columnIndex);
        }
    }

    public void addElement(String itemName) {
        int index = this.items.size();
        this.items.add(new OverrideItem(itemName));
        fireTableRowsInserted(index, index);
    }

    public void removeElement(String itemName) {
        for (int i=0;i<items.size();i++){
            if (items.get(i).getName().equals(itemName)){
                items.remove(i);
                fireTableRowsDeleted(i,i );
                break;
            }
        }
    }

    public void changeElement(String oldItemName, String itemName) {
        for (int i=0;i<items.size();i++){
            if (items.get(i).getName().equals(oldItemName)){
                items.get(i).setName(itemName);
                fireTableCellUpdated(i,1);
                break;
            }
        }
    }
}
