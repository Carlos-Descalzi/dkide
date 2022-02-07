package io.datakitchen.ide.editors.neweditors.action;

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

public class ActionNodeModelReader {
    private final ActionNodeModelImpl model;
    private final VirtualFile nodeFolder;
    private final ComponentSource componentSource;

    public ActionNodeModelReader(ActionNodeModelImpl model, VirtualFile nodeFolder, ComponentSource componentSource){
        this.model = model;
        this.nodeFolder = nodeFolder;
        this.componentSource = componentSource;
    }

    public void read() throws IOException, ParseException {
        readDescription();
        readNotebook();
        readDataSources();
    }

    private void readDataSources() throws IOException, ParseException {
        VirtualFile dataSourcesFolder = nodeFolder.findChild("actions");
        if (dataSourcesFolder != null) {
            for (VirtualFile dataSourceFile : dataSourcesFolder.getChildren()) {
                readDataSource(dataSourceFile);
            }
        }
    }

    private void readDataSource(VirtualFile dataSourceFile) throws IOException, ParseException {
        Map<String, Object> dataSource = JsonUtil.read(dataSourceFile);

        String name = dataSourceFile.getName().replace(".json","");
        String configRef = (String)dataSource.get("config-ref");

        Map<String, Connector> connectorMap = ConnectorUtil.getConnectors(componentSource)
                .stream().collect(Collectors.toMap(Connector::getName, c->c));

        Connector connector = connectorMap.get(configRef);

        ConnectionImpl connection = new ConnectionImpl(model, (ActionConnectionList)model.getConnectionList(), name, connector);

        String wildcardPrefix = (String)dataSource.get("wildcard-key-prefix");
        String wildcard = (String)dataSource.get("wildcard");

        if (StringUtils.isNotBlank(wildcardPrefix)
                && StringUtils.isNotBlank(wildcard)){

            String wildcardExpr = wildcardPrefix+wildcard;

            connection.addKey(new ActionKey(connection, wildcardExpr));
        }

        Map<String, Object> keys = ObjectUtil.cast(dataSource.get("keys"));

        for (Map.Entry<String, Object> entry: keys.entrySet()){
            Key key = parseSourceKey(entry, connection);
            if (key != null) {
                connection.addKey(key);
            }
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
        ((ActionConnectionList)model.getConnectionList()).addConnection(connection);
    }

    private Key parseSourceKey(Map.Entry<String, Object> entry, ConnectionImpl connection) {

        ConnectorNature nature = connection.getConnector().getConnectorType().getNature();

        Map<String, Object> keyConfig = ObjectUtil.cast(entry.getValue());

        Key key = null;
        if (nature == ConnectorNature.SQL) {
            key = ActionKey.fromJson(connection, keyConfig);
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


    private void readNotebook() throws IOException, ParseException {
        Map<String, Object> notebookJson = JsonUtil.read(
                Objects.requireNonNull(nodeFolder.findChild(Constants.FILE_NOTEBOOK_JSON)));

        Map<String, Object> tests = ObjectUtil.cast(notebookJson.get("tests"));

        if (tests != null) {
            for (Map.Entry<String, Object> entry : tests.entrySet()) {
                Map<String, Object> testJson = ObjectUtil.cast(entry.getValue());
                Test test = Test.fromJson(testJson, new LinkedHashMap<>());
                model.addTest(test);
            }
        }
    }

    private void readDescription() throws IOException, ParseException {
        Map<String, Object> descriptionJson = JsonUtil.read(
                Objects.requireNonNull(nodeFolder.findChild(Constants.FILE_DESCRIPTION_JSON)));

        model.setDescription((String)descriptionJson.get("description"));
    }
}
