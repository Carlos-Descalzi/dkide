package io.datakitchen.ide.model;

import io.datakitchen.ide.Constants;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public enum ConnectorType {

    POSTGRESQL( ConnectorNature.SQL,"PostgreSQL"),
    MYSQL(ConnectorNature.SQL,"MYSQL"),
    MSSQL(ConnectorNature.SQL,"MSSQL"),
    ORACLE(ConnectorNature.SQL,"Oracle"),
    DB2(ConnectorNature.SQL,"DB2"),
    BIGQUERY(ConnectorNature.SQL,"BigQuery"),
    SNOWFLAKE(ConnectorNature.SQL,"Snowflake"),
    S3(ConnectorNature.FILE,"S3"),
    ADLS2(ConnectorNature.FILE,"ADLS2"),
    AZUREBLOB(ConnectorNature.FILE,"AzureBlob"),
    SFTP(ConnectorNature.FILE,"SFTP"),
    FTP(ConnectorNature.FILE,"FTP"),
    VERTICA(ConnectorNature.SQL,"Vertica"),
    SALESFORCE(ConnectorNature.SQL,"Salesforce"),
    TERADATA(ConnectorNature.SQL,"Teradata"),
    DICTIONARY(ConnectorNature.DICT, "Dictionary");

    private final String name;

    private final ConnectorNature nature;

    ConnectorType(ConnectorNature nature, String name){
        this.nature = nature;
        this.name = name;
    }

    public static ConnectorType fromSchema(String schema) {
        for (ConnectorType type:values()){
            if (type.getSchema().equals(schema)){
                return type;
            }
        }
        return null;
    }

    public ConnectorNature getNature() {
        return nature;
    }

    public String getName(){
        return name;
    }

    public String getDescription(){
        return name+" Connector";
    }

    public String getSchema(){
        return Constants.SCHEMA_PREFIX+name+"Config";
    }

    public boolean isWildcardSupported(){
        return nature == ConnectorNature.FILE;
    }

    public static final Set<String> SCHEMAS = Arrays.stream(ConnectorType.values()).map(ConnectorType::getSchema).collect(Collectors.toSet());
}
