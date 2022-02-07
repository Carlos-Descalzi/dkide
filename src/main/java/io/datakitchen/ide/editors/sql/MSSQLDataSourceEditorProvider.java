package io.datakitchen.ide.editors.sql;

public class MSSQLDataSourceEditorProvider extends SQLDataSourceEditorProvider {

    @Override
    protected String getTypeName() {
        return "DKDataSource_MSSQL";
    }

    @Override
    protected Class<? extends SqlDataSourceEditor> getEditorClass() {
        return MSSQLDataSourceEditor.class;
    }

}
