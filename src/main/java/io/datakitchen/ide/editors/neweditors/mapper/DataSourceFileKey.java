package io.datakitchen.ide.editors.neweditors.mapper;

import io.datakitchen.ide.model.*;
import io.datakitchen.ide.ui.EventSupport;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class DataSourceFileKey implements FileKey {

    private final EventSupport<KeyListener> listeners = EventSupport.of(KeyListener.class);

    private final ConnectionImpl connection;
    private final String name;
    private String file;
    private final Set<RuntimeVariable> variables = new LinkedHashSet<>();

    public DataSourceFileKey(ConnectionImpl connection, String name,String file) {
        this.connection = connection;
        this.name = name;
        this.file = file;
    }

    public int hashCode(){
        return new HashCodeBuilder()
                .append(file)
                .toHashCode();
    }

    public boolean equals(Object other){
        return other instanceof DataSourceFileKey
                && new EqualsBuilder()
                .append(file, ((DataSourceFileKey) other).file)
                .isEquals();
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

    public String toString(){
        return file;
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
    public Map<String, Object> toJson() {
        Map<String, Object> json = new LinkedHashMap<>();

        json.put("file-key", file);
        json.put("use-only-file-key", true);

        if (!variables.isEmpty()) {
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
        return file;
    }

    @Override
    public boolean isWildcard() {
        return file.contains("*");
    }

    @Override
    public Set<RuntimeVariable> getVariables() {
        return variables;
    }

}
