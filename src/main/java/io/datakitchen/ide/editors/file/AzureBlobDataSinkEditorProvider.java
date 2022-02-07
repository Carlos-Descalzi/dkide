package io.datakitchen.ide.editors.file;

public class AzureBlobDataSinkEditorProvider extends FileDataEditorProvider {

    @Override
    protected String getTypeName() {
        return "DKDataSink_AzureBlob";
    }

    @Override
    protected Class<? extends FileDataEditor> getEditorClass() {
        return AzureBlobDataSinkEditor.class;
    }
}
