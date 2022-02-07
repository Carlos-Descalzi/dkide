package io.datakitchen.ide.editors.neweditors.mapper;

import io.datakitchen.ide.model.*;
import io.datakitchen.ide.ui.EventSupport;
import org.apache.commons.lang.StringUtils;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class DataSinkSqlKey implements SQLKey, SinkKey {

    private final EventSupport<KeyListener> listeners = EventSupport.of(KeyListener.class);

    private String name;
    private final Key sourceKey;
    private DumpType dumpType;
    private final ConnectionImpl connection;
    private String tableName;
    private final Set<RuntimeVariable> variables = new LinkedHashSet<>();
    private CsvOptions csvOptions;

    public DataSinkSqlKey(ConnectionImpl connection, String name, Key sourceKey, String tableName){
        this.name = name;
        this.connection = connection;
        this.sourceKey = sourceKey;
        this.tableName = tableName;
        setDumpType(sourceKey instanceof DataSourceSqlKey ? ((DataSourceSqlKey) sourceKey).getDumpType() : DumpType.CSV );
    }

    public static DataSinkSqlKey fromJson(ConnectionImpl connection, String name, Key sourceKey, Map<String, Object> json){

        DataSinkSqlKey key = new DataSinkSqlKey(connection, name, sourceKey, (String)json.get("table-name"));
        if (sourceKey instanceof DataSourceSqlKey){
            key.setCsvOptions(((DataSourceSqlKey)sourceKey).getCsvOptions());
            key.setDumpType(((DataSourceSqlKey) sourceKey).getDumpType());
        }
        return key;
    }

    public String toString(){
        return tableName;
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
    public Key getSourceKey() {
        return sourceKey;
    }

    @Override
    public DumpType getDumpType() {
        return dumpType;
    }

    @Override
    public void setDumpType(DumpType dumpType) {
        this.dumpType = dumpType;
        if (sourceKey instanceof DataSourceSqlKey){
            ((DataSourceSqlKey)sourceKey).setDumpType(dumpType);
        }
    }

    @Override
    public Connection getConnection() {
        return connection;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
        listeners.getProxy().keyChanged(new KeyEvent(this));
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        listeners.getProxy().keyChanged(new KeyEvent(this));
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
    public String getQueryFile() {
        return null;
    }

    @Override
    public void setQueryFile(String queryFile) {

    }

    @Override
    public QueryType getQueryType() {
        return null;
    }

    @Override
    public void setQueryType(QueryType queryType) {

    }

    public CsvOptions getCsvOptions() {
        return csvOptions;
    }

    public void setCsvOptions(CsvOptions csvOptions) {
        this.csvOptions = csvOptions;
        if (sourceKey instanceof DataSourceSqlKey){
            ((DataSourceSqlKey)sourceKey).setCsvOptions(csvOptions);
        }
        listeners.getProxy().keyChanged(new KeyEvent(this));
    }

    @Override
    public Map<String, Object> toJson() {
        Map<String, Object> json = new LinkedHashMap<>();

        json.put("query-type", "bulk_insert");
        json.put("table-name", tableName);
        json.put("format", dumpType.getIdentifier());

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

    @Override
    public String getDescription() {
        return tableName;
    }

}
