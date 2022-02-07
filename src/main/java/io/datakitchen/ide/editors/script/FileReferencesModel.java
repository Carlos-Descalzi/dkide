package io.datakitchen.ide.editors.script;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

public abstract class FileReferencesModel extends AbstractTableModel {
    protected List<FileReference> fileReferences = new ArrayList<>();

    @Override
    public int getRowCount() {
        return fileReferences.size();
    }

    @Override
    public int getColumnCount() {
        return 3;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return true;
    }

    public void addFile() {
        int index = fileReferences.size();
        fileReferences.add(new FileReference());
        fireTableRowsInserted(index, index);
    }

    public void removeFile(int index) {
        fileReferences.remove(index);
        fireTableRowsDeleted(index, index);
    }

    public List<FileReference> getFileReferences() {
        return fileReferences;
    }

    public void setFileReferences(List<FileReference> fileReferences) {
        this.fileReferences = fileReferences;
        fireTableDataChanged();
    }
}
