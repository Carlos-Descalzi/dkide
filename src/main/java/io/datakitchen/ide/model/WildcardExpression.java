package io.datakitchen.ide.model;

import java.util.ArrayList;
import java.util.List;

public class WildcardExpression {
    private String prefix;
    private String pattern;

    public WildcardExpression(String prefix, String pattern){
        this.prefix = prefix;
        this.pattern = pattern;
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String toString(){
        return String.join("/",prefix,pattern);
    }

    public static WildcardExpression fromString(String path){
        String[] tokens = path.split("/");
        List<String> prefix = new ArrayList<>();
        List<String> pattern = new ArrayList<>();
        int i=0;
        for (;i<tokens.length;i++){
            if (tokens[i].contains("*")){
                break;
            }
            prefix.add(tokens[i]);
        }

        for (;i<tokens.length;i++){
            pattern.add(tokens[i]);
        }

        return new WildcardExpression(String.join("/",prefix)+"/", String.join("/", pattern));
    }
}
