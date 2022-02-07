package io.datakitchen.ide.editors.script;

import io.datakitchen.ide.editors.DsInfo;
import org.apache.commons.lang3.StringUtils;

import java.util.LinkedHashMap;
import java.util.Map;

public class FileReference {
    private DsInfo sourceSink;
    private String key;

    private String fileName;

    public DsInfo getSourceSink() {
        return sourceSink;
    }

    public void setSourceSink(DsInfo sourceSink) {
        this.sourceSink = sourceSink;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public boolean isValid() {
        return sourceSink != null
                && StringUtils.isNotBlank(key)
                && StringUtils.isNotBlank(fileName);
    }

    public Map<String, Object> toJson(){
        Map<String,Object> entry = new LinkedHashMap<>();
        entry.put("filename",fileName);
        entry.put("key",sourceSink.getName()+"."+key);
        return entry;
    }
}
