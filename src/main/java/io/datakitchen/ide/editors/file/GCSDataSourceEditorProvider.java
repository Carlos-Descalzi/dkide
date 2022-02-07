package io.datakitchen.ide.editors.file;

public class GCSDataSourceEditorProvider extends FileDataEditorProvider {

    @Override
    protected String getTypeName() {
        return "DKDataSource_GCS";
    }

    @Override
    protected Class<? extends FileDataEditor> getEditorClass() {
        return GCSDataSourceEditor.class;
    }
}
