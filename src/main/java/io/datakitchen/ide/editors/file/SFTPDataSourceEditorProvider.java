package io.datakitchen.ide.editors.file;

public class SFTPDataSourceEditorProvider extends FileDataEditorProvider {

    @Override
    protected String getTypeName() {
        return "DKDataSource_SFTP";
    }

    @Override
    protected Class<? extends FileDataEditor> getEditorClass() {
        return SFTPDataSourceEditor.class;
    }
}
