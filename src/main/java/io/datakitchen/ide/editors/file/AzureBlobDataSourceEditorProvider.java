package io.datakitchen.ide.editors.file;

public class AzureBlobDataSourceEditorProvider extends FileDataEditorProvider {

    @Override
    protected String getTypeName() {
        return "DKDataSource_AzureBlob";
    }

    @Override
    protected Class<? extends FileDataEditor> getEditorClass() {
        return AzureBlobDataSourceEditor.class;
    }
}
