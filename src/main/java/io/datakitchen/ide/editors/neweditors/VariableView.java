package io.datakitchen.ide.editors.neweditors;

import io.datakitchen.ide.model.Key;
import io.datakitchen.ide.model.RuntimeVariable;
import io.datakitchen.ide.ui.LabelWithActions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class VariableView extends LabelWithActions implements PropertyChangeListener {

    private final RuntimeVariable variable;
    private final Key key;

    public VariableView(RuntimeVariable variable, ActionSupplier<VariableView> actionSupplier){
        this(variable, null, actionSupplier);
    }

    public VariableView(RuntimeVariable variable, Key key, ActionSupplier<VariableView> actionSupplier){
        super("", null /*EditorIcons.VARIABLE*/, actionSupplier);
        this.variable = variable;
        this.variable.addPropertyChangeListener(this);
        this.key = key;

        updateView();
    }

    private void updateView() {
        String text = variable.getVariableName()+" = "+ variable.getAttribute().getDisplayName()
                + (key != null ? " of "+ key.getName() : "");
        setText(text);
        setToolTipText("<html><pre>"+text+"</pre></html>");
    }

    public RuntimeVariable getVariable() {
        return variable;
    }

    public Key getKey() {
        return key;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        updateView();
    }
}
