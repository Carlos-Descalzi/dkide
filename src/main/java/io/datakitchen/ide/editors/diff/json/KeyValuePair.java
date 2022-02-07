package io.datakitchen.ide.editors.diff.json;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.List;

public class KeyValuePair<K, V> extends AbstractJsonNode{
    private K key;
    private V value;

    public KeyValuePair(JsonNode parent, K key, V value) {
        super(parent);
        this.key = key;
        this.value = value;
    }

    public String toString(){
        return "KeyValuePair:("+key+"="+(value instanceof List ? "(list)" : value)+")";
    }

    public K getKey() {
        return key;
    }

    public void setKey(K key) {
        this.key = key;
    }

    public V getValue() {
        return value;
    }

    public void setValue(V value) {
        this.value = value;
    }

    public boolean equals(Object other){
        return other instanceof KeyValuePair
            && new EqualsBuilder()
//            .append(getParent(), ((KeyValuePair)other).getParent())
            .append(key, ((KeyValuePair)other).key)
            .append(value, ((KeyValuePair) other).value)
            .isEquals();
    }

    public int hashCode(){
        return new HashCodeBuilder()
//            .append(getParent())
            .append(key)
            .append(value)
            .toHashCode();
    }
}
