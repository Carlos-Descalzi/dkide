package io.datakitchen.ide.editors.file;

public class S3DataSinkEditorProvider extends FileDataEditorProvider {

    @Override
    protected String getTypeName() {
        return "DKDataSink_S3";
    }

    @Override
    protected Class<? extends FileDataEditor> getEditorClass() {
        return S3DataSinkEditor.class;
    }
}
