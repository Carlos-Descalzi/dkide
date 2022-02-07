package io.datakitchen.ide.editors.neweditors.container;

import io.datakitchen.ide.model.Connection;
import io.datakitchen.ide.model.KeyEvent;
import io.datakitchen.ide.model.KeyListener;
import io.datakitchen.ide.model.RuntimeVariable;
import io.datakitchen.ide.ui.EventSupport;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class DataSinkFileKey implements DataSinkKey{

    private final EventSupport<KeyListener> listeners = EventSupport.of(KeyListener.class);

    private final ConnectionImpl connection;
    private final String name;
    private String containerFileName;
    private String outputFileName;
    private final Set<RuntimeVariable> variables = new LinkedHashSet<>();

    public DataSinkFileKey(ConnectionImpl connection, String name, String containerFileName, String outputFileName){
        this.connection = connection;
        this.name = name;
        this.containerFileName = containerFileName;
        this.outputFileName = outputFileName;
    }

    public String getOutputFileName() {
        return outputFileName;
    }

    public void setOutputFileName(String outputFileName) {
        this.outputFileName = outputFileName;
    }

    @Override
    public void addKeyListener(KeyListener listener) {
        listeners.addListener(listener);
    }

    @Override
    public void removeKeyListener(KeyListener listener) {
        listeners.removeListener(listener);
    }

    @Override
    public Connection getConnection() {
        return connection;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void addVariable(RuntimeVariable variable) {
        this.variables.add(variable);
        listeners.getProxy().variableAdded(new KeyEvent(this, variable));
    }

    @Override
    public void removeVariable(RuntimeVariable variable) {
        this.variables.remove(variable);
        listeners.getProxy().variableRemoved(new KeyEvent(this, variable));
    }

    @Override
    public Set<RuntimeVariable> getVariables() {
        return variables;
    }

    public String getContainerFileName() {
        return containerFileName;
    }

    public void setContainerFileName(String containerFileName) {
        this.containerFileName = containerFileName;
    }

    public String toString(){
        return "docker-share/"+containerFileName + " -> "+outputFileName;
    }

    @Override
    public Map<String, Object> toJson() {
        Map<String, Object> json = new LinkedHashMap<>();
        json.put("file-key", outputFileName);
        json.put("use-only-file-key", true);

        if (!variables.isEmpty()){
            Map<String, Object> variablesJson = new LinkedHashMap<>();

            for (RuntimeVariable variable: variables){
                variablesJson.put(variable.getAttribute().getName(), variable.getVariableName());
            }

            json.put("set-runtime-vars", variablesJson);
        }

        return json;
    }

    @Override
    public String getDescription() {
        return outputFileName;
    }
}
