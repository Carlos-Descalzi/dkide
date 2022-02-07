package io.datakitchen.ide.editors.file;

public class ADLS2DataSinkEditorProvider extends FileDataEditorProvider {

    @Override
    protected String getTypeName() {
        return "DKDataSink_ADLS2";
    }

    @Override
    protected Class<? extends FileDataEditor> getEditorClass() {
        return ADLS2DataSinkEditor.class;
    }
}
