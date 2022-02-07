package io.datakitchen.ide.editors.neweditors;

import io.datakitchen.ide.model.Test;

public class TestsHolder extends ConnectionItemContainer<TestView> {
    public TestsHolder(){
        super("Tests");
    }

    public void removeViewForTest(Test test){
        remove((t)->t.getTest().equals(test));
    }

}
