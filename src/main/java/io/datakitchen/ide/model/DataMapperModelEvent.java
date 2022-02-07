package io.datakitchen.ide.model;

import java.util.Collection;
import java.util.EventObject;

public class DataMapperModelEvent extends EventObject {

    private String propertyName;
    private Object oldValue;
    private Object newValue;
    private Collection<Mapping> mappings;

    public DataMapperModelEvent(Object source) {
        super(source);
    }

    public DataMapperModelEvent(Object source, Collection<Mapping> mappings) {
        super(source);
        this.mappings = mappings;
    }

    public DataMapperModelEvent(Object source, String propertyName, Object oldValue, Object newValue) {
        super(source);
        this.propertyName = propertyName;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    public Collection<Mapping> getMappings() {
        return mappings;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public Object getOldValue() {
        return oldValue;
    }

    public Object getNewValue() {
        return newValue;
    }
}
