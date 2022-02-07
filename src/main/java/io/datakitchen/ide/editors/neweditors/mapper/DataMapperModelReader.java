package io.datakitchen.ide.editors.neweditors.mapper;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import io.datakitchen.ide.Constants;
import io.datakitchen.ide.editors.neweditors.FileTest;
import io.datakitchen.ide.editors.neweditors.palette.ComponentSource;
import io.datakitchen.ide.editors.neweditors.palette.ConnectorUtil;
import io.datakitchen.ide.model.*;
import io.datakitchen.ide.util.JsonUtil;
import net.minidev.json.parser.ParseException;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings({"unchecked","rawtypes"})
public class DataMapperModelReader {

    private final Project project;
    private final DataMapperModelImpl model;

    public DataMapperModelReader(Project project, DataMapperModelImpl model){
        this.project = project;
        this.model = model;
    }

    public void read(VirtualFile nodeFolder, ComponentSource componentSource) throws Exception{
        Map<String, Connector> connectorMap = ConnectorUtil
                .getConnectors(componentSource)
                .stream().collect(Collectors.toMap(Connector::getName, c -> c));
        readDescription(nodeFolder);
        readDataSources(nodeFolder, connectorMap);
        List<ConnectionMapping> mappings = readNotebook(nodeFolder);
        readDataSinks(nodeFolder, connectorMap, mappings);
    }

    private void readDescription(VirtualFile nodeFolder) throws IOException, ParseException {
        VirtualFile descriptionFile = nodeFolder.findChild(Constants.FILE_DESCRIPTION_JSON);
        assert descriptionFile != null;
        Map<String, Object> descriptionJson = JsonUtil.read(descriptionFile);
        model.setDescription((String)descriptionJson.get("description"));
    }

    private void readDataSources(VirtualFile nodeFolder, Map<String, Connector> connectorMap) throws IOException, ParseException {
        VirtualFile dataSourcesFolder = nodeFolder.findChild("data_sources");

        assert dataSourcesFolder != null;
        for (VirtualFile dsFile: dataSourcesFolder.getChildren()){
            readDataSource(dsFile, connectorMap);
        }
    }

    private void readDataSinks(VirtualFile nodeFolder, Map<String, Connector> connectorMap, List<ConnectionMapping> mappings) throws IOException, ParseException {
        VirtualFile dataSinksFolder = nodeFolder.findChild("data_sinks");

        for (VirtualFile dsFile: dataSinksFolder.getChildren()){
            readDataSink(dsFile, connectorMap, mappings);
        }
    }

    private void readDataSink(VirtualFile dsFile, Map<String, Connector> connectorMap, List<ConnectionMapping> mappings) throws IOException, ParseException {
        Map<String, Object> dataSource = JsonUtil.read(dsFile);

        String name = dsFile.getName().replace(".json","");
        String configRef = (String)dataSource.get("config-ref");

        Connector connector = connectorMap.get(configRef);

        ConnectionImpl connection = new ConnectionImpl(model,(DataMapperConnectionList)model.getDataSinks(), name, connector);

        Map<String, Object> keys = (Map<String, Object>) dataSource.get("keys");

        for (Map.Entry<String, Object> entry: keys.entrySet()){
            connection.addKey(parseSinkKey(entry, connection, mappings));
        }

        mappings.stream()
            .filter(m -> m.sinkName.equals(connection.getName()) && m.sinkKey == null)
            .findFirst()
            .ifPresent(wildcardMapping -> {
                Connection sourceConnection = model.getDataSources().getConnections().stream()
                        .filter(c -> c.getName().equals(wildcardMapping.getSourceName()))
                        .findFirst()
                        .get();

                Key wildcardKey = sourceConnection.getKeys().stream()
                        .filter(k -> k instanceof DataSourceFileKey
                                && ((DataSourceFileKey)k).isWildcard()
                        ).findFirst()
                        .get();

                String wildcardKeyPrefix = (String) dataSource.get("wildcard-key-prefix");
                String wildcard = (String) dataSource.get("wildcard");

                DataSinkFileKey key = new DataSinkFileKey(connection, wildcardKeyPrefix+wildcard,wildcardKey);
                key.setFile(wildcardKeyPrefix+wildcard);

                connection.addKey(key);
            }
        );

        for (RuntimeVariable variable: readRuntimeVariables(dataSource)){
            connection.addVariable(variable);
        }

        Map<String, RuntimeVariable> variables = collectAllVariables(connection);

        Map<String, Object> tests = (Map<String, Object>) dataSource.get("tests");

        if (tests != null) {
            for (Map.Entry<String, Object> entry : tests.entrySet()) {
                connection.addTest(parseTest(entry, connection, variables));
            }
        }

        ((DataMapperConnectionList)model.getDataSinks()).addConnection(connection);
    }

    private void readDataSource(VirtualFile dsFile, Map<String, Connector> connectorMap) throws IOException, ParseException {
        Map<String, Object> dataSource = JsonUtil.read(dsFile);

        String name = dsFile.getName().replace(".json","");
        String configRef = (String)dataSource.get("config-ref");

        Connector connector = connectorMap.get(configRef);

        ConnectionImpl connection = new ConnectionImpl(model, (DataMapperConnectionList)model.getDataSources(), name, connector);

        String wildcardPrefix = (String)dataSource.get("wildcard-key-prefix");
        String wildcard = (String)dataSource.get("wildcard");

        if (StringUtils.isNotBlank(wildcardPrefix)
            && StringUtils.isNotBlank(wildcard)){

            String wildcardExpr = wildcardPrefix+wildcard;

            connection.addKey(new DataSourceFileKey(connection, wildcardExpr, wildcardExpr));
        }

        Map<String, Object> keys = (Map<String, Object>) dataSource.get("keys");

        for (Map.Entry<String, Object> entry: keys.entrySet()){
            connection.addKey(parseSourceKey(entry, connection));
        }

        for (RuntimeVariable variable: readRuntimeVariables(dataSource)){
            connection.addVariable(variable);
        }

        Map<String, RuntimeVariable> variables = collectAllVariables(connection);

        Map<String, Object> tests = (Map<String, Object>) dataSource.get("tests");

        if (tests != null) {
            for (Map.Entry<String, Object> entry : tests.entrySet()) {
                connection.addTest(parseTest(entry, connection, variables));
            }
        }
        ((DataMapperConnectionList)model.getDataSources()).addConnection(connection);
    }

    private Key parseSourceKey(Map.Entry<String, Object> entry, ConnectionImpl connection) {
        ConnectorNature nature = connection.getConnector().getConnectorType().getNature();

        Map<String, Object> keyConfig = (Map<String, Object>) entry.getValue();

        Key key = null;
        if (nature == ConnectorNature.SQL){
            key = DataSourceSqlKey.fromJson(connection, entry.getKey(), keyConfig);
        } else if (nature == ConnectorNature.FILE){
            key = new DataSourceFileKey(connection, entry.getKey(), (String)keyConfig.get("file-key"));
        }

        if (key != null){
            for (RuntimeVariable variable: readRuntimeVariables(keyConfig)){
                key.addVariable(variable);
            }
        }

        return key;
    }

    private Key parseSinkKey(Map.Entry<String, Object> entry, ConnectionImpl connection, List<ConnectionMapping> mappings) {
        ConnectorNature nature = connection.getConnector().getConnectorType().getNature();

        String keyName = entry.getKey();

        ConnectionMapping mapping = mappings.stream()
                .filter(c -> c.sinkName.equals(connection.getName()) && keyName.equals(c.sinkKey))
                .findFirst()
                .orElse(null);

        if (mapping != null){
            Connection dataSource = model.getDataSources()
                    .getConnections()
                    .stream().filter(c -> c.getName().equals(mapping.sourceName))
                    .findFirst()
                    .orElse(null);

            if (dataSource != null){
                Key sourceKey = dataSource.getKeys()
                        .stream().filter(k -> k.getName().equals(mapping.sourceKey))
                        .findFirst()
                        .orElse(null);

                Map<String, Object> keyConfig = (Map<String, Object>) entry.getValue();
                Key key = null;
                if (nature == ConnectorNature.SQL){
                    key = DataSinkSqlKey.fromJson(connection, entry.getKey(),sourceKey, keyConfig);
                } else if (nature == ConnectorNature.FILE){
                    key = new DataSinkFileKey(connection, entry.getKey(),sourceKey, (String)keyConfig.get("file-key"));
                }

                if (key != null){
                    for (RuntimeVariable variable: readRuntimeVariables(keyConfig)){
                        key.addVariable(variable);
                    }
                }

                return key;
            }

        }
        return null;
    }

    private Set<RuntimeVariable> readRuntimeVariables(Map<String, Object> source){
        Set<RuntimeVariable> result = new LinkedHashSet<>();
        Map<String, String> runtimeVars = (Map<String, String>)source.get("set-runtime-vars");
        if (runtimeVars != null){
            for (Map.Entry<String, String> varEntry: runtimeVars.entrySet()){
                result.add(new RuntimeVariable(
                        VariableDescription.fromName(varEntry.getKey()),
                        varEntry.getValue()
                ));
            }
        }
        return result;
    }

    private Map<String, RuntimeVariable> collectAllVariables(ConnectionImpl connection) {
        Map<String, RuntimeVariable> variableMap = new LinkedHashMap<>();
        for (RuntimeVariable variable: connection.getVariables()){
            variableMap.put(variable.getVariableName(), variable);
        }
        for (Key key:connection.getKeys()){
            for (RuntimeVariable variable: key.getVariables()){
                variableMap.put(variable.getVariableName(), variable);
            }
        }
        return variableMap;
    }

    private Test parseTest(Map.Entry<String, Object> entry, ConnectionImpl connection, Map<String, RuntimeVariable> variables) {
        Map<String, Object> testData = (Map<String, Object>)entry.getValue();
        String testVariable = (String)testData.get("test-variable");
        if (testVariable.startsWith("testvar_")){
            Key key = findKeyContainingVariable(connection, testVariable);
            return FileTest.fromJson(key, testData, variables);
        } else {
            return Test.fromJson(testData, variables);
        }
    }

    private Key findKeyContainingVariable(ConnectionImpl connection, String testVariable) {
        for (Key key: connection.getKeys()){
            for (RuntimeVariable variable:key.getVariables()){
                if (variable.getVariableName().equals(testVariable)){
                    return key;
                }
            }
        }
        return null;
    }


    private List<ConnectionMapping> readNotebook(VirtualFile nodeFolder) throws IOException, ParseException {
        VirtualFile notebookFile = nodeFolder.findChild(Constants.FILE_NOTEBOOK_JSON);
        assert notebookFile != null;
        Map<String, Object> notebook = JsonUtil.read(notebookFile);

        List<ConnectionMapping> mappingList = new ArrayList<>();

        Map<String, Object> mappings = (Map<String, Object>) notebook.get("mappings");

        if (mappings != null) {
            for (Map.Entry<String, Object> entry : mappings.entrySet()) {
                Map<String, String> mapping = (Map<String, String>) entry.getValue();
                mappingList.add(new ConnectionMapping(
                        mapping.get("source-name"),
                        mapping.get("source-key"),
                        mapping.get("sink-name"),
                        mapping.get("sink-key")
                ));
            }
        }
        List<Map<String, String>> wildcardMapping = (List<Map<String, String>>) notebook.get("wildcard-will-automatically-create-mappings");

        if (wildcardMapping != null){
            for (Map<String, String> mapping: wildcardMapping){
                mappingList.add(new ConnectionMapping(
                        mapping.get("data-source"),
                        null,
                        mapping.get("data-sink"),
                        null
                ));
            }
        }

        return mappingList;
    }

    private static class ConnectionMapping {
        private final String sourceName;
        private final String sourceKey;
        private final String sinkName;
        private final String sinkKey;

        public ConnectionMapping(String sourceName, String sourceKey, String sinkName, String sinkKey) {
            this.sourceName = sourceName;
            this.sourceKey = sourceKey;
            this.sinkName = sinkName;
            this.sinkKey = sinkKey;
        }

        public String getSourceName() {
            return sourceName;
        }

        public String getSourceKey() {
            return sourceKey;
        }

        public String getSinkName() {
            return sinkName;
        }

        public String getSinkKey() {
            return sinkKey;
        }
    }
}
