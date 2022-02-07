package io.datakitchen.ide.editors.variables;

import io.datakitchen.ide.util.JsonUtil;
import net.minidev.json.JSONArray;

import java.util.Map;

public class Variable {
    public enum Type {
        STRING,
        NUMBER,
        BOOLEAN,
        JSON
    }

    private Type type = Type.STRING;
    private String name = "";
    private String value = "";

    public Variable(){}
    public Variable(String name, Object data){
        this.name = name;
        if (data instanceof Map){
            value = JsonUtil.toJsonString((Map<String,Object>)data);
            type = Type.JSON;
        } else if (data instanceof JSONArray){
            value = JsonUtil.toJsonString((JSONArray)data);
            type = Type.JSON;
        } else if (data instanceof String){
            value = (String)data;
            type = Type.STRING;
        } else if (data instanceof Number){
            value = String.valueOf(data);
            type = Type.NUMBER;
        } else if (data instanceof Boolean){
            value = String.valueOf(data);
            type = Type.BOOLEAN;
        }
    }

    public Object getJsonValue(){
        if (type == Type.STRING){
            return value;
        } else if (type == Type.BOOLEAN){
            return Boolean.valueOf(value);
        } else if (type == Type.NUMBER){
            if (value.contains(".")){
                return Float.valueOf(value);
            }
            return Integer.valueOf(value);
        } else {
            try {
                return JsonUtil.read(value);
            } catch (Exception ex){
                return value;
            }
        }
    }

    public String toString() {
        return name == null || name.equals("") ? "(new variable)" : name;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
