package io.datakitchen.ide.editors.file;

public class FTPDataSinkEditorProvider extends FileDataEditorProvider {

    @Override
    protected String getTypeName() {
        return "DKDataSink_FTP";
    }

    @Override
    protected Class<? extends FileDataEditor> getEditorClass() {
        return FTPDataSinkEditor.class;
    }
}
