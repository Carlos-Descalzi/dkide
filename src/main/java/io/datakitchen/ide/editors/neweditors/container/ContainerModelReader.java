package io.datakitchen.ide.editors.neweditors.container;

import com.intellij.openapi.vfs.VirtualFile;
import io.datakitchen.ide.Constants;
import io.datakitchen.ide.editors.neweditors.FileTest;
import io.datakitchen.ide.editors.neweditors.palette.ComponentSource;
import io.datakitchen.ide.editors.neweditors.palette.ConnectorUtil;
import io.datakitchen.ide.model.*;
import io.datakitchen.ide.util.JsonUtil;
import io.datakitchen.ide.util.ObjectUtil;
import net.minidev.json.parser.ParseException;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class ContainerModelReader {
    private final VirtualFile nodeFolder;

    public ContainerModelReader(VirtualFile nodeFolder){
        this.nodeFolder = nodeFolder;
    }

    protected VirtualFile getNodeFolder(){
        return nodeFolder;
    }

    public void read(ContainerModelImpl model, ComponentSource componentSource) throws Exception {
        Map<String, Connector> connectorMap = ConnectorUtil
                .getConnectors(componentSource)
                .stream().collect(Collectors.toMap(Connector::getName, c -> c));

        readDescription(model, nodeFolder);

        ContainerFiles files = readNotebook(model);
        readDataSources(model, connectorMap, files);
        readDataSinks(model, connectorMap, files);
    }

    private void readDescription(ContainerModelImpl model, VirtualFile nodeFolder) throws IOException, ParseException {
        VirtualFile descriptionFile = nodeFolder.findChild(Constants.FILE_DESCRIPTION_JSON);
        assert descriptionFile != null;
        Map<String, Object> descriptionJson = JsonUtil.read(descriptionFile);
        model.setDescription((String)descriptionJson.get("description"));
    }

    private void readDataSources(ContainerModelImpl model, Map<String, Connector> connectorMap, ContainerFiles files) throws IOException, ParseException {
        VirtualFile dataSourcesFolder = nodeFolder.findChild("data_sources");
        if (dataSourcesFolder != null){
            for (VirtualFile file: dataSourcesFolder.getChildren()){
                readDataSource(file, model, connectorMap, files);
            }
        }
    }

    private void readDataSinks(ContainerModelImpl model, Map<String, Connector> connectorMap, ContainerFiles files) throws IOException, ParseException {
        VirtualFile dataSinksFolder = nodeFolder.findChild("data_sinks");
        if (dataSinksFolder != null){
            for (VirtualFile file: dataSinksFolder.getChildren()){
                readDataSink(file, model, connectorMap, files);
            }
        }
    }

    private void readDataSource(VirtualFile file, ContainerModelImpl model, Map<String, Connector> connectorMap, ContainerFiles files) throws IOException, ParseException {

        String name = file.getName().replace(".json","");
        Map<String, Object> dataSource = JsonUtil.read(file);

        String configRef = (String)dataSource.get("config-ref");
        Connector connector = connectorMap.get(configRef);
        ConnectionImpl connection = new ConnectionImpl(model, ((ContainerConnectionList)model.getDataSources()), name, connector);

        Map<String, Object> keys = ObjectUtil.cast(dataSource.get("keys"));

        for (Map.Entry<String, Object> entry: keys.entrySet()){
            connection.addKey(parseSourceKey(entry, connection,files));
        }

        String wildcardKeyPrefix = (String)dataSource.get("wildcard-key-prefix");
        String wildcardPattern = (String)dataSource.get("wildcard");

        if (StringUtils.isNotBlank(wildcardKeyPrefix)
            && StringUtils.isNotBlank(wildcardPattern)){

            String wildcard = String.join("/", wildcardKeyPrefix, wildcardPattern);
            String outputWildcard = files.inputFiles.get(connection.getName()+".*");
            connection.addKey(new DataSourceFileKey(connection, wildcard, wildcard, outputWildcard));
        }

        for (RuntimeVariable variable: readRuntimeVariables(dataSource)){
            connection.addVariable(variable);
        }

        Map<String, RuntimeVariable> variables = collectAllVariables(connection);

        Map<String, Object> tests = ObjectUtil.cast(dataSource.get("tests"));

        if (tests != null) {
            for (Map.Entry<String, Object> entry : tests.entrySet()) {
                connection.addTest(parseTest(entry, connection, variables));
            }
        }

        ((ContainerConnectionList)model.getDataSources()).addConnection(connection);
    }

    private void readDataSink(VirtualFile file, ContainerModelImpl model, Map<String, Connector> connectorMap, ContainerFiles files) throws IOException, ParseException {
        String name = file.getName().replace(".json","");
        Map<String, Object> dataSource = JsonUtil.read(file);

        String configRef = (String)dataSource.get("config-ref");
        Connector connector = connectorMap.get(configRef);
        ConnectionImpl connection = new ConnectionImpl(model, ((ContainerConnectionList)model.getDataSources()), name, connector);

        Map<String, Object> keys = ObjectUtil.cast(dataSource.get("keys"));

        for (Map.Entry<String, Object> entry: keys.entrySet()){
            connection.addKey(parseSinkKey(entry, connection,files));
        }

        Map<String, RuntimeVariable> variables = collectAllVariables(connection);

        Map<String, Object> tests = ObjectUtil.cast(dataSource.get("tests"));

        if (tests != null) {
            for (Map.Entry<String, Object> entry : tests.entrySet()) {
                connection.addTest(parseTest(entry, connection, variables));
            }
        }

        ((ContainerConnectionList)model.getDataSinks()).addConnection(connection);
    }

    private Key parseSinkKey(Map.Entry<String, Object> entry, ConnectionImpl connection, ContainerFiles files) {
        ConnectorNature nature = connection.getConnector().getConnectorType().getNature();

        Map<String, Object> keyConfig = ObjectUtil.cast(entry.getValue());

        Key key = null;

        String outputFile = files.outputFiles.get(connection.getName()+"."+entry.getKey());

        if (nature == ConnectorNature.SQL){
            key = new DataSinkSqlKey(connection, entry.getKey(), outputFile, (String)keyConfig.get("sql-file"));
        } else if (nature == ConnectorNature.FILE){
            key = new DataSinkFileKey(connection, entry.getKey(), outputFile, (String)keyConfig.get("file-key"));
        }

        if (key != null){
            for (RuntimeVariable variable: readRuntimeVariables(keyConfig)){
                key.addVariable(variable);
            }
        }

        return key;
    }

    private Key parseSourceKey(Map.Entry<String, Object> entry, ConnectionImpl connection, ContainerFiles files) {
        ConnectorNature nature = connection.getConnector().getConnectorType().getNature();

        Map<String, Object> keyConfig = ObjectUtil.cast(entry.getValue());

        Key key = null;

        String inputFile = files.inputFiles.get(connection.getName()+"."+entry.getKey());
        if (nature == ConnectorNature.SQL){
            key = new DataSourceSqlKey(connection, entry.getKey(), (String)keyConfig.get("sql-file"), inputFile);
        } else if (nature == ConnectorNature.FILE){
            key = new DataSourceFileKey(connection, entry.getKey(), (String)keyConfig.get("file-key"), inputFile);
        }

        if (key != null){
            for (RuntimeVariable variable: readRuntimeVariables(keyConfig)){
                key.addVariable(variable);
            }
        }

        return key;
    }

    private Set<RuntimeVariable> readRuntimeVariables(Map<String, Object> source){
        Set<RuntimeVariable> result = new LinkedHashSet<>();
        Map<String, String> runtimeVars = ObjectUtil.cast(source.get("set-runtime-vars"));
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

    private Test parseTest(Map.Entry<String, Object> entry, ConnectionImpl connection, Map<String, RuntimeVariable> variables) {
        Map<String, Object> testData = ObjectUtil.cast(entry.getValue());
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

    private ContainerFiles readNotebook(ContainerModelImpl model) throws Exception{
        VirtualFile notebookFile = nodeFolder.findChild(Constants.FILE_NOTEBOOK_JSON);
        assert notebookFile != null;
        Map<String, Object> notebookData = JsonUtil.read(notebookFile);

        String repositoryName = (String)notebookData.get("image-repo");
        String imageTag = (String)notebookData.get("image-tag");
        String namespace = (String)notebookData.get("dockerhub-namespace");

        String imageName = (StringUtils.isNotBlank(namespace) ? namespace+"/" : "")
                + repositoryName
                + ":"+StringUtils.defaultString(imageTag,"latest");

        model.setImageName(imageName);
        model.setUsername((String)notebookData.get("dockerhub-username"));
        model.setPassword((String)notebookData.get("dockerhub-password"));
        model.setRegistry((String)notebookData.get("dockerhub-url"));
        model.setCommand((String)notebookData.get("command-line"));

        List<Map<String,String>> assignments = ObjectUtil.cast(notebookData.get("assign-variables"));

        if (assignments != null) {
            for (Map<String, String> assignment : assignments) {
                model.addAssignment(new Assignment(assignment.get("file"), assignment.get("name")));
            }
        }

        List<Map<String, String>> inputFiles = ObjectUtil.cast(notebookData.get("container-input-file-keys"));

        Map<String, String> inputFileMap;
        if (inputFiles != null) {
            inputFileMap = inputFiles.stream().collect(
                    Collectors.toMap(m -> m.get("key"), m -> m.get("filename"))
            );
        } else {
            inputFileMap = new LinkedHashMap<>();
        }
        List<Map<String, String>> outputFiles = ObjectUtil.cast(notebookData.get("container-output-file-keys"));

        Map<String, String> outputFileMap;
        if (outputFiles != null) {
            outputFileMap = outputFiles.stream().collect(
                    Collectors.toMap(m -> m.get("key"), m -> m.get("filename"))
            );
        } else {
            outputFileMap = new LinkedHashMap<>();
        }

        Map<String, Object> tests = ObjectUtil.cast(notebookData.get("tests"));

        if (tests != null){
            for (Map.Entry<String, Object> entry: tests.entrySet()){
                Map<String, Object> test = ObjectUtil.cast(entry.getValue());
                model.addTest(Test.fromJson(test,new LinkedHashMap<>()));
            }
        }

        return new ContainerFiles(inputFileMap, outputFileMap);
    }

    private static class ContainerFiles {
        Map<String, String> inputFiles;
        Map<String, String> outputFiles;

        public ContainerFiles(Map<String, String> inputFiles, Map<String, String> outputFiles) {
            this.inputFiles = inputFiles;
            this.outputFiles = outputFiles;
        }
    }
}
