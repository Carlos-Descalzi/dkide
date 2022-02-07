package io.datakitchen.ide.editors.sql;

public class TeradataDataSourceEditorProvider extends SQLDataSourceEditorProvider {
    @Override
    protected String getTypeName() {
        return "DKDataSource_Teradata";
    }

    @Override
    protected Class<? extends SqlDataSourceEditor> getEditorClass() {
        return TeradataDataSourceEditor.class;
    }
}
