package io.datakitchen.ide.editors.neweditors;

import io.datakitchen.ide.model.RuntimeVariable;

public class VariablesHolder extends ConnectionItemContainer<VariableView> {
    public VariablesHolder(){
        super("Variables");
    }

    public void removeViewForVariable(RuntimeVariable variable){
        remove((v)->v.getVariable().equals(variable));
    }

    public void removeViewForVariable(String key, RuntimeVariable variable) {
        remove((v)->v.getVariable().equals(variable) && v.getKey().getName().equals(key));
    }
}
