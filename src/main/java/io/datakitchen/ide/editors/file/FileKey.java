package io.datakitchen.ide.editors.file;

import io.datakitchen.ide.ui.NamedObject;
import org.apache.commons.lang3.StringUtils;

import java.util.LinkedHashMap;
import java.util.Map;

public class FileKey implements NamedObject {

    private String name;
    private String filePath;
    private Map<String, Object> runtimeVars = new LinkedHashMap<>();

    public FileKey(){}

    public FileKey(String name, String filePath, Map<String, Object> runtimeVars) {
        this.name = name;
        this.filePath = filePath;
        this.runtimeVars = runtimeVars;
    }

    public FileKey(String name) {
        this.name = name;
    }

    public String toString(){
        return StringUtils.isBlank(name) ? "(no name)" : name;
    }

    public static FileKey fromEntry(Map.Entry<String, Object> entry){
        Map<String, Object> key = (Map<String, Object>)entry.getValue();
        String filePath = (String) key.get("file-key");
        Map<String, Object> runtimeVars = (Map<String, Object>) key.get("set-runtime-vars");
        if (runtimeVars == null){
            runtimeVars = new LinkedHashMap<>();
        }
        return new FileKey(entry.getKey(), filePath, runtimeVars);
    }

    public static FileKey fromJsonData(Map<String, Object> jsonData) {
        String filePath = (String) jsonData.get("file-key");
        if (filePath == null){
            throw new RuntimeException("Invalid data"); // TODO put proper exception here.
        }
        Map<String, Object> runtimeVars = (Map<String, Object>) jsonData.get("set-runtime-vars");
        if (runtimeVars == null){
            runtimeVars = new LinkedHashMap<>();
        }
        return new FileKey(null, filePath, runtimeVars);
    }

    public Map<String, Object> toJson(){
        Map<String, Object> entry = new LinkedHashMap<>();
        entry.put("file-key", filePath);
        entry.put("use-only-file-key", true);
        if (!runtimeVars.isEmpty()){
            entry.put("set-runtime-vars", runtimeVars);
        }
        return entry;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public void setRuntimeVariables(Map<String, Object> runtimeVars) {
        this.runtimeVars = runtimeVars;
    }

    public Map<String, Object> getRuntimeVariables() {
        return runtimeVars;
    }

}
