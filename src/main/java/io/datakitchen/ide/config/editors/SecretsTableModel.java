package io.datakitchen.ide.config.editors;

import io.datakitchen.ide.config.Secret;
import org.bouncycastle.util.Arrays;

import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

public class SecretsTableModel extends AbstractTableModel {
    private static final String[] COLUMN_NAMES = {"Path", "Value"};

    private List<Secret> secrets = new ArrayList<>();

    public void setSecrets(List<Secret> secrets) {
        this.secrets = secrets;
        if (this.secrets == null) {
            this.secrets = new ArrayList<>();
        }
        fireTableStructureChanged();
    }

    public List<Secret> getSecrets() {
        return secrets;
    }

    @Override
    public int getRowCount() {
        return secrets.size();
    }

    @Override
    public int getColumnCount() {
        return 2;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Secret secret = secrets.get(rowIndex);
        switch (columnIndex) {
            case 0:
                return secret.getPath();
            case 1:
                return secret.getValue();
        }
        return null;
    }

    public void addSecret() {
        int index = secrets.size();
        secrets.add(new Secret());
        fireTableRowsInserted(index, index);
    }

    public void removeSecret(int index) {
        secrets.remove(index);
        fireTableRowsDeleted(index, index);
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
        Secret secret = secrets.get(rowIndex);
        switch (columnIndex) {
            case 0:
                secret.setPath((String) aValue);
                break;
            case 1:
                secret.setValue((String) aValue);
                break;
        }
    }

    public void removeSecrets(int[] indices) {
        indices = Arrays.reverse(indices);
        for (int index: indices){
            secrets.remove(index);
        }
        fireTableChanged(new TableModelEvent(this));
    }
}
