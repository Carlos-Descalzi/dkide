package io.datakitchen.ide.editors.file;

public class S3DataSourceEditorProvider extends FileDataEditorProvider {

    @Override
    protected String getTypeName() {
        return "DKDataSource_S3";
    }

    @Override
    protected Class<? extends FileDataEditor> getEditorClass() {
        return S3DataSourceEditor.class;
    }
}
