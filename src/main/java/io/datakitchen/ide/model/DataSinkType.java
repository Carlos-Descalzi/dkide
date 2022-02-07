package io.datakitchen.ide.model;


import java.util.Set;

public enum DataSinkType implements DataType{

    POSTGRESQL(ConnectorType.POSTGRESQL, VariableDescription.SQL_CONN_VARS,VariableDescription.SQL_SINK_KEY_VARS),
    MYSQL(ConnectorType.MYSQL, VariableDescription.SQL_CONN_VARS,VariableDescription.SQL_SINK_KEY_VARS),
    MSSQL(ConnectorType.MSSQL, VariableDescription.SQL_CONN_VARS,VariableDescription.SQL_SINK_KEY_VARS),
    ORACLE(ConnectorType.ORACLE, VariableDescription.SQL_CONN_VARS,VariableDescription.SQL_SINK_KEY_VARS),
    DB2(ConnectorType.DB2, VariableDescription.SQL_CONN_VARS,VariableDescription.SQL_SINK_KEY_VARS),
    BIGQUERY(ConnectorType.BIGQUERY, VariableDescription.SQL_CONN_VARS,VariableDescription.SQL_SINK_KEY_VARS),
    SNOWFLAKE(ConnectorType.SNOWFLAKE, VariableDescription.SQL_CONN_VARS,VariableDescription.SQL_SINK_KEY_VARS),
    TERADATA(ConnectorType.TERADATA, VariableDescription.SQL_CONN_VARS,VariableDescription.SQL_SINK_KEY_VARS),
    S3(ConnectorType.S3, VariableDescription.FILE_CONN_VARS, VariableDescription.FILE_KEY_VARS),
    ADLS2(ConnectorType.ADLS2, VariableDescription.FILE_CONN_VARS,VariableDescription.FILE_KEY_VARS),
    AZUREBLOB(ConnectorType.AZUREBLOB, VariableDescription.FILE_CONN_VARS,VariableDescription.FILE_KEY_VARS),
    SFTP(ConnectorType.SFTP, VariableDescription.FILE_CONN_VARS, VariableDescription.FILE_KEY_VARS),
    FTP(ConnectorType.FTP, VariableDescription.FILE_CONN_VARS, VariableDescription.FILE_KEY_VARS),
    DICTIONARY(ConnectorType.DICTIONARY, Set.of(), Set.of());

    private final ConnectorType connectorType;
    private final Set<VariableDescription> connectorVariables;
    private final Set<VariableDescription> keyVariables;

    DataSinkType(ConnectorType connectorType, Set<VariableDescription> connectorVariables, Set<VariableDescription> keyVariables){
        this.connectorType = connectorType;
        this.connectorVariables = connectorVariables;
        this.keyVariables = keyVariables;
    }

    public static DataSinkType forConnectorType(ConnectorType connectorType) {
        for (DataSinkType t:values()){
            if (t.getConnectorType().equals(connectorType)){
                return t;
            }
        }
        return null;
    }

    public ConnectorType getConnectorType(){
        return connectorType;
    }

    public String getTypeName(){
        return "DKDataSink_"+connectorType.getName();
    }

    @Override
    public Set<VariableDescription> getConnectorVariables() {
        return connectorVariables;
    }

    @Override
    public Set<VariableDescription> getKeyVariables() {
        return keyVariables;
    }

}
