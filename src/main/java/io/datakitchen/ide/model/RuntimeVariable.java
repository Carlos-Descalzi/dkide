package io.datakitchen.ide.model;

import io.datakitchen.ide.ui.EventSupport;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class RuntimeVariable {
    private final EventSupport<PropertyChangeListener> listeners = EventSupport.of(PropertyChangeListener.class);
    private VariableDescription attribute;
    private String variableName;

    public RuntimeVariable() {
    }

    public RuntimeVariable(VariableDescription attribute, String variableName) {
        this.attribute = attribute;
        this.variableName = variableName;
    }

    public void addPropertyChangeListener(PropertyChangeListener listener){
        this.listeners.addListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener){
        this.listeners.removeListener(listener);
    }

    private void firePropertyChange(String propertyName, Object oldValue, Object newValue){
        this.listeners.getProxy().propertyChange(new PropertyChangeEvent(this, propertyName, oldValue, newValue));
    }

    public VariableDescription getAttribute() {
        return attribute;
    }

    public void setAttribute(VariableDescription attribute) {
        VariableDescription oldAttribute = this.attribute;
        this.attribute = attribute;
        firePropertyChange("attribute", oldAttribute, attribute);
    }

    public String getVariableName() {
        return variableName;
    }

    public void setVariableName(String variableName) {
        String oldVariableName = this.variableName;
        this.variableName = variableName;
        firePropertyChange("variableName", oldVariableName, variableName);
    }

    public boolean isTestVariable(){
        return StringUtils.isBlank(variableName) || variableName.startsWith("testvar_");
    }

    public boolean equals(Object other){
        return other instanceof RuntimeVariable
            && new EqualsBuilder()
            .append(attribute, ((RuntimeVariable) other).attribute)
            .append(variableName, ((RuntimeVariable) other).variableName)
            .isEquals();
    }

    public int hashCode(){
        return new HashCodeBuilder()
                .append(attribute)
                .append(variableName)
                .toHashCode();
    }
}
