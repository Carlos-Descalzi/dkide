package io.datakitchen.ide.editors.sql;

public class MSSQLDataSinkEditorProvider extends SQLDataSinkEditorProvider {

    @Override
    protected String getTypeName() {
        return "DKDataSink_MSSQL";
    }

    @Override
    protected Class<? extends SQLDataSinkEditor> getEditorClass() {
        return MSSQLDataSinkEditor.class;
    }
}
