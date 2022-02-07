package io.datakitchen.ide.editors.sql;

public class VerticaDataSinkEditorProvider extends SQLDataSinkEditorProvider {

    @Override
    protected String getTypeName() {
        return "DKDataSink_Vertica";
    }

    @Override
    protected Class<? extends SQLDataSinkEditor> getEditorClass() {
        return VerticaDataSinkEditor.class;
    }
}
