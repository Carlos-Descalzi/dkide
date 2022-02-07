package io.datakitchen.ide.editors.sql;

public class SnowflakeDataSourceEditorProvider extends SQLDataSourceEditorProvider {
    @Override
    protected String getTypeName() {
        return "DKDataSource_Snowflake";
    }

    @Override
    protected Class<? extends SqlDataSourceEditor> getEditorClass() {
        return SnowflakeDataSourceEditor.class;
    }
}
