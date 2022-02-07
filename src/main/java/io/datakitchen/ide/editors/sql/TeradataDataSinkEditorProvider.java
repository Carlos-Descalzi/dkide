package io.datakitchen.ide.editors.sql;

public class TeradataDataSinkEditorProvider extends SQLDataSinkEditorProvider {

    @Override
    protected String getTypeName() {
        return "DKDataSink_Teradata";
    }

    @Override
    protected Class<? extends SQLDataSinkEditor> getEditorClass() {
        return TeradataDataSinkEditor.class;
    }
}
