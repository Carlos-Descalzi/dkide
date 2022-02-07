package io.datakitchen.ide.json;

public class VariableReference {
    private final String variable;

    public VariableReference(String variable){
        this.variable = variable;
    }

    public String getVariable() {
        return variable;
    }

    public boolean equals(Object other){
        return other instanceof VariableReference
                && variable.equals((((VariableReference) other).variable));
    }

    public int hashCode(){
        return variable.hashCode();
    }

    public String toString(){
        return "{{"+variable+"}}";
    }
}
