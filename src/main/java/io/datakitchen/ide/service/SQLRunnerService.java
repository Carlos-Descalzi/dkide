package io.datakitchen.ide.service;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import io.datakitchen.ide.config.ConfigurationService;
import io.datakitchen.ide.model.Connector;
import io.datakitchen.ide.model.ConnectorType;
import io.datakitchen.ide.tools.DatabaseConfiguration;

import java.io.File;
import java.sql.*;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class SQLRunnerService implements Disposable {
    private static final Map<String, ConnectionInfo> CONN_MAP = new LinkedHashMap<>();
    static {
        CONN_MAP.put(ConnectorType.POSTGRESQL.getName(), new ConnectionInfo(
                "com.amazon.redshift.jdbc42.Driver",
                "jdbc:redshift://{{hostname}}/{{database}}?ssl=true&UID={{username}}&PWD={{password}}&IniFile="
                        +System.getProperty("java.io.tmpdir")+ File.separator+ "redshift.ini"
        ));
        CONN_MAP.put(ConnectorType.MSSQL.getName(), new ConnectionInfo(
                "net.sourceforge.jtds.jdbc.Driver",
                "jdbc:jtds:sqlserver://{{hostname}}:{{port}}/{{database}}"
        ));
        CONN_MAP.put(ConnectorType.SNOWFLAKE.getName(), new ConnectionInfo(
                "net.snowflake.client.jdbc.SnowflakeDriver",
                "jdbc:snowflake:{{account}}.snowflakecomputing.com:/?warehouse={{warehouse}}"
        ));
        CONN_MAP.put(ConnectorType.DB2.getName(), new ConnectionInfo(
                "com.ibm.db2.jcc.DB2Driver",
                "jdbc:db2:{{hostname}}:{{port}}/{{database}}"
        ));
        CONN_MAP.put(ConnectorType.MYSQL.getName(), new ConnectionInfo(
                "com.mysql.jdbc.Driver",
                "jdbc:mysql:{{hostname}}:{{port}}/{{database}}"
        ));
        CONN_MAP.put(ConnectorType.TERADATA.getName(), new ConnectionInfo(
                "com.teradata.jdbc.TeraDriver",
                "jdbc:teradata:{{hostname}}"
        ));
    }

    private final Project project;
    private final Map<String, Connection> connections = new HashMap<>();
    private final Map<Connector, Connection> connectorsByConnector = new HashMap<>();

    public SQLRunnerService(Project project){
        this.project = project;

        try {
            // Workaround for issue with Redshift JDBC driver.
            new File(System.getProperty("java.io.tmpdir"),"redshift.ini").createNewFile();
        }catch(Exception ex){}
    }

    private void closeAll(){
        for (Connection connection: connections.values()){
            try {
                connection.close();
            }catch (Exception ignore){}
        }
        for (Connection connection: connectorsByConnector.values()){
            try {
                connection.close();
            }catch(Exception ignore){}
        }
    }

    public static SQLRunnerService getInstance(Project project){
        return project.getService(SQLRunnerService.class);
    }

    public ResultSet execute(String connectorName, String sql) throws SQLException {
        Connection connection = getConnection(connectorName);

        return doExecute(sql, connection);
    }

    public ResultSet execute(Connector connector, String sql) throws SQLException {
        Connection connection = getConnection(connector);

        return doExecute(sql, connection);
    }

    private ResultSet doExecute(String sql, Connection connection) throws SQLException {
        Statement statement = connection.createStatement();

        String[] statements = sql.split(";"); // TODO improve this

        for (String stmt : statements) {
            statement.execute(stmt);
        }
        return statement.getResultSet();
    }

    private Connection getConnection(Connector connector) throws SQLException {
        Connection conn = connectorsByConnector.get(connector);
        if (conn == null){
            conn = createConnection(connector);
            connectorsByConnector.put(connector,conn);
        }
        return conn;
    }

    private Connection createConnection(Connector connector) throws SQLException {
        Map<String, String> secrets = ConfigurationService
            .getInstance(project).getSecrets().stream().collect(
            Collectors.toMap(s -> s.getPath(), s -> s.getValue())
        );
        ConnectionInfo info = CONN_MAP.get(connector.getConnectorType().getName());

        Map<String, Object> configuration = resolveVaultSecrets(connector.getConfig(), secrets);

        return info.createConnection(configuration);

    }

    private Map<String, Object> resolveVaultSecrets(Map<String, Object> config, Map<String, String> secrets) {
        Map<String, Object> result = new LinkedHashMap<>();

        for (Map.Entry<String, Object> entry: config.entrySet()){
            result.put(entry.getKey(), resolveSecret(secrets, entry.getValue()));
        }
        return result;
    }

    private Object resolveSecret(Map<String, String> secrets, Object entry) {
        if (entry instanceof String) {
            String stringEntry = (String) entry;
            if (stringEntry.contains("vault://")) {
                return secrets.get(stringEntry.replace("#{vault://", "").replace("}", ""));
            }
        }
        return entry;
    }

    public Connection getConnection(String connectorName) throws SQLException {
        if (!connections.containsKey(connectorName)) {
            for (DatabaseConfiguration config : ConfigurationService.getInstance(project).getConnections()) {
                if (config.getName().equals(connectorName)) {
                    connections.put(connectorName,config.connect());
                }
            }
        }
        return connections.get(connectorName);
    }


    @Override
    public void dispose() {
        closeAll();
    }
}
