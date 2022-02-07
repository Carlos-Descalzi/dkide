package io.datakitchen.ide.editors.sql;

public class BigQueryDataSourceEditorProvider extends SQLDataSourceEditorProvider {

    @Override
    protected String getTypeName() {
        return "DKDataSource_BigQuery";
    }

    @Override
    protected Class<? extends SqlDataSourceEditor> getEditorClass() {
        return BigQueryDataSourceEditor.class;
    }

}
