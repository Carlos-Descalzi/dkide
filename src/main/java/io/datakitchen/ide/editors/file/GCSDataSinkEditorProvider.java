package io.datakitchen.ide.editors.file;

public class GCSDataSinkEditorProvider extends FileDataEditorProvider {

    @Override
    protected String getTypeName() {
        return "DKDataSink_GCS";
    }

    @Override
    protected Class<? extends FileDataEditor> getEditorClass() {
        return GCSDataSinkEditor.class;
    }
}
