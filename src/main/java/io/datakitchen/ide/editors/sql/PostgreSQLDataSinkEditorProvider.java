package io.datakitchen.ide.editors.sql;

public class PostgreSQLDataSinkEditorProvider extends SQLDataSinkEditorProvider {

    @Override
    protected String getTypeName() {
        return "DKDataSink_PostgreSQL";
    }

    @Override
    protected Class<? extends SQLDataSinkEditor> getEditorClass() {
        return PostgreSQLDataSinkEditor.class;
    }
}
