package io.datakitchen.ide.editors.script;

import io.datakitchen.ide.editors.DsInfo;

public class OutputFilesModel extends FileReferencesModel {

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        if (columnIndex == 2 && getFileReferences().get(rowIndex).getSourceSink() == null) {
            return false;
        }
        return true;
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        FileReference file = fileReferences.get(rowIndex);
        switch (columnIndex) {
            case 0:
                file.setFileName((String) aValue);
                break;
            case 1:
                file.setSourceSink((DsInfo) aValue);
                break;
            case 2:
                file.setKey((String) aValue);
                break;
        }
        fireTableCellUpdated(rowIndex, columnIndex);
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        FileReference file = fileReferences.get(rowIndex);
        switch (columnIndex) {
            case 0:
                return file.getFileName();
            case 1:
                return file.getSourceSink();
            case 2:
                return file.getKey();

        }
        return null;
    }

}
