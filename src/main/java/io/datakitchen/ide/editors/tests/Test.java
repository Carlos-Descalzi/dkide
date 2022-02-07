package io.datakitchen.ide.editors.tests;

import io.datakitchen.ide.ui.NamedObject;
import org.apache.commons.lang3.StringUtils;

import java.util.LinkedHashMap;
import java.util.Map;

public class Test implements NamedObject {
    private String name;
    private Map<String,Object> test;

    public Test(String name) {
        this.name = name;
        this.test = new LinkedHashMap<>();
        test.put("test-logic","");
    }

    public String toString(){
        return StringUtils.isBlank(name) ? "(no name)" : name;
    }

    public Test(){
        test = new LinkedHashMap<>();
        test.put("test-logic","");
    }

    public Test(String name, Map<String,Object> test) {
        this.name = name;
        this.test = test;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String,Object> getTest() {
        return test;
    }

    public void setTest(Map<String,Object> test) {
        this.test = test;
    }

    public boolean isValid() {
        return StringUtils.isNotBlank(name)
                && test != null
                && StringUtils.isNotBlank((String)test.get("test-variable"))
                && StringUtils.isNotBlank((String)test.get("action"))
                && StringUtils.isNotBlank((String)test.get("type"))
                && test.get("test-logic") != null; // TODO finish

    }

    public static Test fromEntry(Map.Entry<String, Object> entry) {
        return new Test(entry.getKey(), (Map<String, Object>) entry.getValue());
    }
}
