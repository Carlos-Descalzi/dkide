package io.datakitchen.ide.editors.script;

import org.apache.commons.lang.StringUtils;

import java.util.LinkedHashMap;
import java.util.Map;

public class VariableAssignment {
    private String variable;
    private String filename;

    public VariableAssignment(){}

    public VariableAssignment(String variable, String filename) {
        this.variable = variable;
        this.filename = filename;
    }

    public String getVariable() {
        return variable;
    }

    public void setVariable(String variable) {
        this.variable = variable;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public boolean isValid() {
        return StringUtils.isNotBlank(variable)
                && StringUtils.isNotBlank(filename);
    }

    public static VariableAssignment fromJson(Map<String, Object> obj){
        return new VariableAssignment(
                (String)obj.get("name"),
                (String)obj.get("file"));
    }

    public Map<String, Object> toJson() {
        Map<String,Object> entry = new LinkedHashMap<>();
        entry.put("name",variable);
        entry.put("file",filename);
        return entry;
    }
}
