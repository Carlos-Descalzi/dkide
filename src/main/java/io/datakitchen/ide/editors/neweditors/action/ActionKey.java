package io.datakitchen.ide.editors.neweditors.action;

import io.datakitchen.ide.model.*;
import io.datakitchen.ide.ui.EventSupport;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ActionKey implements Key {

    private final EventSupport<KeyListener> listeners = EventSupport.of(KeyListener.class);
    private final Set<RuntimeVariable> variables = new LinkedHashSet<>();
    private final ConnectionImpl connection;
    private final String file;
    private QueryType queryType = QueryType.EXECUTE_STATEMENT;

    public ActionKey(ConnectionImpl connection, String file){
        this.connection = connection;
        this.file = file;
    }

    public static ActionKey fromJson(ConnectionImpl connection, Map<String, Object> keyConfig) {
        ActionKey actionKey = new ActionKey(connection, (String)keyConfig.get("sql-file"));

        actionKey.setQueryType(
            QueryType.fromKey(
                (String)keyConfig.getOrDefault("query-type", "execute_non_query")
            )
        );

        DataSourceType dsType = (DataSourceType) connection.getDataType();
        Map<String, VariableDescription> variables = dsType
                .getKeyVariables().stream()
                .collect(Collectors.toMap(VariableDescription::getName, v -> v));

        Map<String, String> runtimeVariablesJson = (Map<String, String>) keyConfig.get("set-runtime-vars");

        if (runtimeVariablesJson != null){
            for (Map.Entry<String, String> entry: runtimeVariablesJson.entrySet()){
                actionKey.addVariable(new RuntimeVariable(variables.get(entry.getKey()), entry.getValue()));
            }
        }

        return actionKey;
    }

    public Map<String, Object> toJson(){
        Map<String, Object> keyData = new LinkedHashMap<>();
        keyData.put("sql-file", file);
        keyData.put("query-type", queryType.getKey());

        if (!variables.isEmpty()){
            Map<String, String> runtimeVariables = new LinkedHashMap<>();
            for (RuntimeVariable variable: variables) {
                runtimeVariables.put(variable.getAttribute().getName(), variable.getVariableName());
            }
            keyData.put("set-runtime-vars",runtimeVariables);
        }

        return keyData;
    }

    @Override
    public String getDescription() {
        return file;
    }

    @Override
    public void addKeyListener(KeyListener listener) {
        this.listeners.addListener(listener);
    }

    @Override
    public void removeKeyListener(KeyListener listener) {
        this.listeners.removeListener(listener);
    }

    @Override
    public Connection getConnection() {
        return connection;
    }

    @Override
    public String getName() {
        return file;
    }

    @Override
    public void addVariable(RuntimeVariable variable) {
        if (variables.add(variable)){
            listeners.getProxy().variableAdded(new KeyEvent(this, variable));
        }
    }

    @Override
    public void removeVariable(RuntimeVariable variable) {
        if (variables.remove(variable)){
            listeners.getProxy().variableRemoved(new KeyEvent(this, variable));
        }
    }

    public QueryType getQueryType() {
        return queryType;
    }

    public void setQueryType(QueryType queryType) {
        this.queryType = queryType;
        listeners.getProxy().keyChanged(new KeyEvent(this));
    }

    @Override
    public Set<RuntimeVariable> getVariables() {
        return variables;
    }

    public String toString(){
        return file +
                (queryType == QueryType.EXECUTE_SCALAR
                        ? " (scalar result)"
                        : "(no results)"
                );
    }
}
