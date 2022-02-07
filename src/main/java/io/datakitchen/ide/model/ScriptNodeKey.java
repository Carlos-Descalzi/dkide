package io.datakitchen.ide.model;

import io.datakitchen.ide.ui.EventSupport;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ScriptNodeKey {
    private final EventSupport<PropertyChangeListener> listeners = EventSupport.of(PropertyChangeListener.class);
    private String script;
    private Map<String, Object> parameters = new LinkedHashMap<>();
    private Map<String, String> environment = new LinkedHashMap<>();
    private List<String> exports = new ArrayList<>();

    public void addPropertyChangeListener(PropertyChangeListener listener){
        listeners.addListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener){
        listeners.removeListener(listener);
    }

    private void firePropertyChange(String propertyName, Object oldValue, Object newValue){
        listeners.getProxy().propertyChange(new PropertyChangeEvent(this, propertyName, oldValue, newValue));
    }

    public String getScript() {
        return script;
    }

    public void setScript(String script) {
        String oldScript = this.script;
        this.script = script;
        firePropertyChange("script", oldScript, script);
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, Object> parameters) {
        Map<String, Object> oldValue = this.parameters;
        this.parameters = parameters;
        firePropertyChange("parameters", oldValue, parameters);
    }

    public Map<String, String> getEnvironment() {
        return environment;
    }

    public void setEnvironment(Map<String, String> environment) {
        Map<String, String> oldValue = this.environment;
        this.environment = environment;
        firePropertyChange("environment", oldValue, environment);
    }

    public List<String> getExports() {
        return exports;
    }

    public void setExports(List<String> exports) {
        List<String> oldValue = this.exports;
        this.exports = exports;
        firePropertyChange("exports", oldValue, exports);
    }
}
