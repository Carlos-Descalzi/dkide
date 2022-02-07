package io.datakitchen.ide.editors.neweditors.container;

import io.datakitchen.ide.model.*;
import io.datakitchen.ide.ui.EventSupport;
import org.apache.commons.lang.StringUtils;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class DataSinkSqlKey implements DataSinkKey{

    private final EventSupport<KeyListener> listeners = EventSupport.of(KeyListener.class);

    private final ConnectionImpl connection;
    private final String name;
    private String containerFileName;
    private String tableName;
    private CsvOptions csvOptions;
    private final Set<RuntimeVariable> variables = new LinkedHashSet<>();

    public DataSinkSqlKey(ConnectionImpl connection, String name, String containerFileName, String tableName){
        this.connection = connection;
        this.name = name;
        this.containerFileName = containerFileName;
        this.tableName = tableName;
    }

    @Override
    public void addKeyListener(KeyListener listener) {
        listeners.addListener(listener);
    }

    @Override
    public void removeKeyListener(KeyListener listener) {
        listeners.removeListener(listener);
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
        listeners.getProxy().keyChanged(new KeyEvent(this));
    }

    @Override
    public String getContainerFileName() {
        return containerFileName;
    }

    @Override
    public void setContainerFileName(String containerFileName) {
        this.containerFileName = containerFileName;
        listeners.getProxy().keyChanged(new KeyEvent(this));
    }

    public CsvOptions getCsvOptions() {
        return csvOptions;
    }

    public void setCsvOptions(CsvOptions csvOptions) {
        this.csvOptions = csvOptions;
        listeners.getProxy().keyChanged(new KeyEvent(this));
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

    @Override
    public String getDescription() {
        return tableName;
    }

    public String toString(){
        return "docker-share/"+containerFileName+" -> "+tableName;
    }

    @Override
    public Map<String, Object> toJson() {
        Map<String, Object> json = new LinkedHashMap<>();

        json.put("query-type", "bulk_insert");
        json.put("table-name", tableName);
        json.put("format", DumpType.JSON.getIdentifier());

        if (!getVariables().isEmpty()) {
            Map<String, Object> variablesJson = new LinkedHashMap<>();

            for (RuntimeVariable variable: getVariables()){
                variablesJson.put(variable.getAttribute().getName(), variable.getVariableName());
            }

            json.put("set-runtime-vars", variablesJson);
        }

        if (csvOptions != null){
            if (StringUtils.isNotBlank(csvOptions.getColumnDelimiter())){
                json.put("col-delimiter", csvOptions.getColumnDelimiter());
            }
            if (StringUtils.isNotBlank(csvOptions.getRowDelimiter())){
                json.put("row-delimiter", csvOptions.getRowDelimiter());
            }
            if (csvOptions.isTitles()) {
                json.put("first-row-column-names", csvOptions.isTitles());
            }
        }
        return json;
    }
}
