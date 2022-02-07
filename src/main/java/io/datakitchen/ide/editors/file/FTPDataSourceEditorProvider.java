package io.datakitchen.ide.editors.file;

public class FTPDataSourceEditorProvider extends FileDataEditorProvider {

    @Override
    protected String getTypeName() {
        return "DKDataSource_FTP";
    }

    @Override
    protected Class<? extends FileDataEditor> getEditorClass() {
        return FTPDataSourceEditor.class;
    }
}
