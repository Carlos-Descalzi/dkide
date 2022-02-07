package io.datakitchen.ide.editors.sql;

public class SnowflakeDataSinkEditorProvider extends SQLDataSinkEditorProvider {

    @Override
    protected String getTypeName() {
        return "DKDataSink_Snowflake";
    }

    @Override
    protected Class<? extends SQLDataSinkEditor> getEditorClass() {
        return SnowflakeDataSinkEditor.class;
    }
}
