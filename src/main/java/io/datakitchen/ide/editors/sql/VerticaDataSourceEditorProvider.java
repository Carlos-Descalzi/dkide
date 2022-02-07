package io.datakitchen.ide.editors.sql;

public class VerticaDataSourceEditorProvider extends SQLDataSourceEditorProvider {
    @Override
    protected String getTypeName() {
        return "DKDataSource_Vertica";
    }

    @Override
    protected Class<? extends SqlDataSourceEditor> getEditorClass() {
        return VerticaDataSourceEditor.class;
    }
}
