package io.datakitchen.ide.config;

import java.io.Serializable;

public class Secret implements Serializable {

    private static final long serialVersionUID = -1;

    private String path;
    private String value;

    public Secret(){}

    public String toString(){
        return path;
    }

    public Secret(String path, String value){
        this.path = path;
        this.value = value;
    }

    public boolean equals(Object other){
        return other instanceof Secret
                && path.equals(((Secret)other).path);
    }

    public int hashCode(){
        return path.hashCode();
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
