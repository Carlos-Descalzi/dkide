package io.datakitchen.ide.editors.file;

public class SFTPDataSinkEditorProvider extends FileDataEditorProvider {

    @Override
    protected String getTypeName() {
        return "DKDataSink_SFTP";
    }

    @Override
    protected Class<? extends FileDataEditor> getEditorClass() {
        return SFTPDataSinkEditor.class;
    }
}
