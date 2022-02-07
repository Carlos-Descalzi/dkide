package io.datakitchen.ide.editors.script;

import io.datakitchen.ide.editors.DsInfo;

public class InputFilesModel extends FileReferencesModel {

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        if (columnIndex == 1 && fileReferences.get(rowIndex).getSourceSink() == null) {
            return false;
        }
        return true;
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        FileReference file = fileReferences.get(rowIndex);
        switch (columnIndex) {
            case 0:
                file.setSourceSink((DsInfo) aValue);
                break;
            case 1:
                file.setKey((String) aValue);
                break;
            case 2:
                file.setFileName((String) aValue);
                break;
        }
        fireTableCellUpdated(rowIndex,columnIndex);
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        FileReference file = fileReferences.get(rowIndex);
        switch (columnIndex) {
            case 0:
                return file.getSourceSink();
            case 1:
                return file.getKey();
            case 2:
                return file.getFileName();

        }
        return null;
    }

}
