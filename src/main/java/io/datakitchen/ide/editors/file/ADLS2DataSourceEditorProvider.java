package io.datakitchen.ide.editors.file;

public class ADLS2DataSourceEditorProvider extends FileDataEditorProvider {

    @Override
    protected String getTypeName() {
        return "DKDataSource_ADLS2";
    }

    @Override
    protected Class<? extends FileDataEditor> getEditorClass() {
        return ADLS2DataSourceEditor.class;
    }
}
