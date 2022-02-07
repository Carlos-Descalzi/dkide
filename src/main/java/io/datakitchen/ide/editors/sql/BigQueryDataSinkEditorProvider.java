package io.datakitchen.ide.editors.sql;

public class BigQueryDataSinkEditorProvider extends SQLDataSinkEditorProvider {

    @Override
    protected String getTypeName() {
        return "DKDataSink_BigQuery";
    }

    @Override
    protected Class<? extends SQLDataSinkEditor> getEditorClass() {
        return BigQueryDataSinkEditor.class;
    }
}
