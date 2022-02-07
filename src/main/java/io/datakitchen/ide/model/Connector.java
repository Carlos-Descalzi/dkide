package io.datakitchen.ide.model;

import io.datakitchen.ide.ui.EventSupport;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.awt.datatransfer.DataFlavor;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Connector implements Serializable {

    private final EventSupport<PropertyChangeListener> listeners = EventSupport.of(PropertyChangeListener.class);

    public static final DataFlavor FLAVOR = new DataFlavor(Connector.class, "Connector");

    private static final long serialVersionUID = 1;

    private String name;
    private final Map<String, Object> config;

    public String toString(){
        return "Connector, name:"+name+", config:"+config;
    }

    public Connector(String name, Map<String, Object> config){
        this.name = name;
        this.config = config;
    }

    public boolean equals(Object other){
        return other instanceof Connector
            && new EqualsBuilder()
                .append(name, ((Connector)other).name)
                .append(config, ((Connector)other).config)
                .isEquals();
    }

    public int hashCode(){
        return new HashCodeBuilder()
                .append(name)
                .append(config)
                .toHashCode();
    }

    public ConnectorType getConnectorType(){
        String schema = (String)config.get("_schema");
        if (schema != null){
            return ConnectorType.fromSchema(schema);
        }
        return null;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        String oldValue = this.name;
        this.name = name;
        listeners.getProxy().propertyChange(new PropertyChangeEvent(this,"name",oldValue,name));
    }

    public Map<String, Object> getConfig() {
        return config;
    }

    public void addPropertyChangeListener(PropertyChangeListener listener){
        this.listeners.addListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener){
        this.listeners.removeListener(listener);
    }

    public static List<Connector> fromVariables(Map<String, Object> variables){
        List<Connector> connectors = new ArrayList<>();
        for (Map.Entry<String, Object> entry:variables.entrySet()){
            Object content = entry.getValue();
            if (validVariable(content)){
                connectors.add(new Connector(entry.getKey(), (Map<String, Object>)content));
            }
        }
        return connectors;
    }


    private static boolean validVariable(Object value){
        if (!(value instanceof Map)){
            return false;
        }
        Map<String, Object> dictVariable = (Map<String, Object>)value;
        String schema = (String)dictVariable.get("_schema");
        return schema != null && ConnectorType.SCHEMAS.contains(schema);
    }


}
