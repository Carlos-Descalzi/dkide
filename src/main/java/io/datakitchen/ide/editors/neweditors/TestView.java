package io.datakitchen.ide.editors.neweditors;

import com.intellij.openapi.util.IconLoader;
import io.datakitchen.ide.model.Test;
import io.datakitchen.ide.ui.LabelWithActions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class TestView extends LabelWithActions implements PropertyChangeListener {

    private final Test test;

    public TestView(Test test, ActionSupplier<TestView> actionSupplier){
        super(test.toString(),IconLoader.getIcon("/"+test.getTestAction().getIconName(),TestView.class), actionSupplier);
        this.test = test;
        this.test.addPropertyChangeListener(this);
        updateView();
    }

    private void updateView(){
        setText(test.toString());
        setIcon(IconLoader.getIcon("/"+test.getTestAction().getIconName(),TestView.class));
        setToolTipText("<html><pre>"+test.toString()+"</pre></html>");
    }

    public Test getTest() {
        return test;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        updateView();
    }
}
