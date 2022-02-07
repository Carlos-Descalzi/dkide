package io.datakitchen.ide.editors.sql;

public class MYSQLDataSourceEditorProvider extends SQLDataSourceEditorProvider {

    @Override
    protected String getTypeName() {
        return "DKDataSource_MYSQL";
    }

    @Override
    protected Class<? extends SqlDataSourceEditor> getEditorClass() {
        return MYSQLDataSourceEditor.class;
    }

}
