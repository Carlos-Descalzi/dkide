package io.datakitchen.ide.editors.neweditors.container;

import io.datakitchen.ide.model.*;
import org.apache.commons.lang.StringUtils;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class DataSourceSqlKey extends BaseDataSourceKey implements SQLKey {

    private final String name;

    private final ConnectionImpl connection;
    private String queryFile;
    private QueryType queryType;

    private DumpType dumpType;
    private final Set<RuntimeVariable> variables = new LinkedHashSet<>();
    private CsvOptions csvOptions;

    public DataSourceSqlKey(ConnectionImpl connection, String name, String queryFile) {
        this.connection = connection;
        this.name = name;
        this.queryFile = queryFile;
        setDumpType(DumpType.CSV);
    }
    public DataSourceSqlKey(ConnectionImpl connection, String name, String queryFile, String containerFileName) {
        this(connection, name, queryFile);
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
    public CsvOptions getCsvOptions() {
        return csvOptions;
    }

    public void setCsvOptions(CsvOptions csvOptions) {
        this.csvOptions = csvOptions;
        listeners.getProxy().keyChanged(new KeyEvent(this));
    }

    @Override
    public Set<RuntimeVariable> getVariables() {
        return variables;
    }

    @Override
    public String getQueryFile() {
        return queryFile;
    }

    @Override
    public void setQueryFile(String queryFile) {
        this.queryFile = queryFile;
        listeners.getProxy().keyChanged(new KeyEvent(this));
    }

    @Override
    public QueryType getQueryType() {
        return queryType;
    }

    @Override
    public void setQueryType(QueryType queryType) {
        this.queryType = queryType;
        listeners.getProxy().keyChanged(new KeyEvent(this));
    }


    public DumpType getDumpType() {
        return dumpType;
    }

    public void setDumpType(DumpType dumpType) {
        this.dumpType = dumpType;
        adjustInputFileName();
        listeners.getProxy().keyChanged(new KeyEvent(this));
    }

    private void adjustInputFileName() {
        String fileName = getContainerFileName();
        if (StringUtils.isBlank(fileName)){
            fileName = queryFile;
        }
        fileName = fileName.substring(0, fileName.indexOf('.')+1)+dumpType.getExtension();
        setContainerFileName(fileName);
    }

    public String toString(){
        return queryFile+" -> docker-share/"+getContainerFileName();
    }

    @Override
    public Map<String, Object> toJson() {
        Map<String, Object> json = new LinkedHashMap<>();

        json.put("query-type", "execute_query");
        json.put("sql-file", queryFile);
        json.put("format", dumpType.getIdentifier());

        if (!getVariables().isEmpty()) {
            Map<String, Object> variablesJson = new LinkedHashMap<>();

            for (RuntimeVariable variable: getVariables()){
                variablesJson.put(variable.getAttribute().getName(), variable.getVariableName());
            }

            json.put("set-runtime-vars", variablesJson);
        }

        if (csvOptions != null){
            Map<String, Object> options = new LinkedHashMap<>();
            if (StringUtils.isNotBlank(csvOptions.getColumnDelimiter())){
                options.put("col-delimiter", csvOptions.getColumnDelimiter());
            }
            if (StringUtils.isNotBlank(csvOptions.getRowDelimiter())){
                options.put("row-delimiter", csvOptions.getRowDelimiter());
            }
            if (csvOptions.isTitles()) {
                options.put("insert-column-names", csvOptions.isTitles());
            }
            json.put("options", options);
        }

        return json;
    }

    @Override
    public String getDescription() {
        return queryFile;
    }
}
