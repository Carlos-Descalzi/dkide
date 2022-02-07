package io.datakitchen.ide.editors.sql;

public class MYSQLDataSinkEditorProvider extends SQLDataSinkEditorProvider {

    @Override
    protected String getTypeName() {
        return "DKDataSink_MYSQL";
    }

    @Override
    protected Class<? extends SQLDataSinkEditor> getEditorClass() {
        return MYSQLDataSinkEditor.class;
    }
}
