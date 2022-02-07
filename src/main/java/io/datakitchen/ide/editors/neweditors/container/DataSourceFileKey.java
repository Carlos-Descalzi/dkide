package io.datakitchen.ide.editors.neweditors.container;

import io.datakitchen.ide.model.*;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class DataSourceFileKey extends BaseDataSourceKey implements FileKey {

    private final ConnectionImpl connection;
    private final String name;
    private String file;
    private final Set<RuntimeVariable> variables = new LinkedHashSet<>();

    public DataSourceFileKey(ConnectionImpl connection, String name, String file, String containerFileName) {
        this.connection = connection;
        this.name = name;
        this.file = file;
        setContainerFileName(containerFileName);
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
    public String getFile() {
        return file;
    }

    @Override
    public void setFile(String file) {
        this.file = file;
        listeners.getProxy().keyChanged(new KeyEvent(this));
    }

    @Override
    public boolean isWildcard() {
        return file.contains("*");
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
        variables.add(variable);
        listeners.getProxy().variableAdded(new KeyEvent(this, variable));
    }

    @Override
    public void removeVariable(RuntimeVariable variable) {
        variables.remove(variable);
        listeners.getProxy().variableRemoved(new KeyEvent(this, variable));
    }

    @Override
    public Set<RuntimeVariable> getVariables() {
        return variables;
    }

    public String toString(){
        return file+" -> docker-share/"+getContainerFileName();
    }

    @Override
    public Map<String, Object> toJson() {
        Map<String, Object> json = new LinkedHashMap<>();
        json.put("file-key", file);
        json.put("use-only-file-key",true);

        if (!getVariables().isEmpty()) {
            Map<String, Object> variablesJson = new LinkedHashMap<>();

            for (RuntimeVariable variable: getVariables()){
                variablesJson.put(variable.getAttribute().getName(), variable.getVariableName());
            }

            json.put("set-runtime-vars", variablesJson);
        }

        return json;
    }

    @Override
    public String getDescription() {
        return file;
    }
}
