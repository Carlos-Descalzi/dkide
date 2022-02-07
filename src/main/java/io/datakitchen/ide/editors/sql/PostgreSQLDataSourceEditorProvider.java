package io.datakitchen.ide.editors.sql;

public class PostgreSQLDataSourceEditorProvider extends SQLDataSourceEditorProvider {

    @Override
    protected String getTypeName() {
        return "DKDataSource_PostgreSQL";
    }

    @Override
    protected Class<? extends SqlDataSourceEditor> getEditorClass() {
        return PostgreSQLDataSourceEditor.class;
    }

}
