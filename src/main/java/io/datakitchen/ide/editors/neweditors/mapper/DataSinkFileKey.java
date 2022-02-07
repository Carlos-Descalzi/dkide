package io.datakitchen.ide.editors.neweditors.mapper;

import io.datakitchen.ide.model.*;
import io.datakitchen.ide.ui.EventSupport;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class DataSinkFileKey implements FileKey, SinkKey {

    private final EventSupport<KeyListener> listeners = EventSupport.of(KeyListener.class);

    private final ConnectionImpl connection;
    private final Key sourceKey;
    private final Set<RuntimeVariable> variables = new LinkedHashSet<>();
    private final String name;
    private String file;
    private DumpType dumpType;

    public DataSinkFileKey(ConnectionImpl connection, String name, Key sourceKey){
        this(connection, name, sourceKey, sourceKey.toString());
    }
    public DataSinkFileKey(ConnectionImpl connection, String name, Key sourceKey, String fileName){
        this.connection = connection;
        this.sourceKey = sourceKey;
        this.name = name;
        this.file = fileName;
        if (sourceKey instanceof DataSourceSqlKey){
            setDumpType(DumpType.CSV);
        } else {
            setDumpType(DumpType.COPY);
        }
    }
    @Override
    public void addKeyListener(KeyListener listener) {
        listeners.addListener(listener);
    }

    @Override
    public void removeKeyListener(KeyListener listener) {
        listeners.removeListener(listener);
    }

    private void adjustNameToDumpType() {
        if (dumpType != DumpType.COPY) {
            file = file.substring(0, file.indexOf('.') + 1) + dumpType.getExtension();
        }
        if (sourceKey instanceof DataSourceSqlKey){
            ((DataSourceSqlKey)sourceKey).setDumpType(dumpType);
        }
    }

    @Override
    public Connection getConnection() {
        return connection;
    }

    public String getName(){
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

    @Override
    public String getFile() {
        return file;
    }

    @Override
    public void setFile(String file) {
        this.file = file;
        listeners.getProxy().keyChanged(new KeyEvent(this));
    }

    public DumpType getDumpType() {
        return dumpType;
    }

    public void setDumpType(DumpType dumpType) {
        this.dumpType = dumpType;
        adjustNameToDumpType();
        listeners.getProxy().keyChanged(new KeyEvent(this));
    }

    @Override
    public Key getSourceKey() {
        return sourceKey;
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

    public String toString(){
        return file;
    }
}
