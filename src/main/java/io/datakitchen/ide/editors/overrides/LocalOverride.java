package io.datakitchen.ide.editors.overrides;

import io.datakitchen.ide.ui.NamedObject;

import java.util.Map;

class LocalOverride implements NamedObject {

    private String name;
    private Object content;
    private String typeName;

    public LocalOverride(){}

    public LocalOverride(String name, Object content){
        this.name = name;
        this.content = content;
        this.typeName = detectType(content);
    }

    public LocalOverride(String name) {
        this.name = name;
        this.content = null;
        this.typeName = "json";
    }

    private String detectType(Object content) {
        if (content instanceof Map){
            Map<String, Object> map = (Map<String, Object>) content;
            if (map.containsKey("_schema")){
                return (String)map.get("_schema");
            }
        }
        return "json";
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    public Object getContent() {
        return content;
    }

    public void setContent(Object content) {
        this.content = content;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }
}
