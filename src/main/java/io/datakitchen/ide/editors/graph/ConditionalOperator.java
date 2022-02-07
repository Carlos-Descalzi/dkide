package io.datakitchen.ide.editors.graph;

public enum ConditionalOperator {
    EQ("equal-to","=="),
    NE("not-equal-to","!="),
    LT("less-than","<"),
    LTE("less-than-equal-to","<="),
    GT("greater-than",">"),
    GTE("greater-than-equal-to",">=");

    private final String definition;
    private final String description;

    ConditionalOperator(String definition, String description){
        this.definition = definition;
        this.description = description;
    }

    public String toString(){
        return description;
    }

    public static ConditionalOperator fromDefinition(String definition) {
        for (ConditionalOperator val:values()){
            if (val.getDefinition().equals(definition)){
                return val;
            }
        }
        return EQ;
    }

    public String getDefinition() {
        return definition;
    }

    public String getDescription() {
        return description;
    }

}
