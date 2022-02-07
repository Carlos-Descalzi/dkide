package io.datakitchen.ide.editors.neweditors.mapper;

import io.datakitchen.ide.model.CsvOptions;
import io.datakitchen.ide.model.DumpType;
import io.datakitchen.ide.model.RuntimeVariable;
import org.apache.commons.lang.StringUtils;

import java.util.LinkedHashMap;
import java.util.Map;

public class DataSourceSqlKey extends DataSourceFileKey {

    private DumpType dumpType;
    private CsvOptions csvOptions;

    public DataSourceSqlKey(ConnectionImpl connection, String name, String file) {
        super(connection, name, file);
        dumpType = DumpType.CSV;
    }

    public static DataSourceSqlKey fromJson(ConnectionImpl connection, String name, Map<String, Object> jsonData){
        DataSourceSqlKey key = new DataSourceSqlKey(connection, name, (String)jsonData.get("sql-file"));

        String format = (String)jsonData.getOrDefault("format", "csv");
        key.setDumpType(DumpType.forType(format));

        String colDelimiter = (String)jsonData.get("col-delimiter");
        String rowDelimiter = (String)jsonData.get("row-delimiter");
        boolean columnNames = (Boolean)jsonData.getOrDefault("insert-column-names", false);

        if (StringUtils.isNotBlank(colDelimiter)
            || StringUtils.isNotBlank(rowDelimiter)
            || columnNames){
            CsvOptions options = new CsvOptions();
            options.setColumnDelimiter(colDelimiter);
            options.setRowDelimiter(rowDelimiter);
            options.setTitles(columnNames);
            key.setCsvOptions(options);
        }

        return key;
    }

    public DumpType getDumpType() {
        return dumpType;
    }

    public void setDumpType(DumpType dumpType) {
        this.dumpType = dumpType;
    }

    public CsvOptions getCsvOptions() {
        return csvOptions;
    }

    public void setCsvOptions(CsvOptions csvOptions) {
        this.csvOptions = csvOptions;
    }

    @Override
    public Map<String, Object> toJson() {
        Map<String, Object> json = new LinkedHashMap<>();

        json.put("query-type", "execute_query");
        json.put("sql-file", getFile());
        json.put("format", dumpType.getIdentifier());

        if (!getVariables().isEmpty()) {
            Map<String, Object> variablesJson = new LinkedHashMap<>();

            for (RuntimeVariable variable: getVariables()){
                variablesJson.put(variable.getAttribute().getName(), variable.getVariableName());
            }

            json.put("set-runtime-vars", variablesJson);
        }

        if (csvOptions != null){
            if (StringUtils.isNotBlank(csvOptions.getColumnDelimiter())) {
                json.put("col-delimiter", csvOptions.getColumnDelimiter());
            }
            if (StringUtils.isNotBlank(csvOptions.getRowDelimiter())) {
                json.put("row-delimiter", csvOptions.getRowDelimiter());
            }
            if (csvOptions.isTitles()) {
                json.put("insert-column-names", csvOptions.isTitles());
            }
        }

        return json;
    }
}
