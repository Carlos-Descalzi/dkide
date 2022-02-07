package io.datakitchen.ide.editors.diff.json;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class KeyValuePairList<K, V> extends ArrayList<KeyValuePair<K,V>> implements JsonNode{

    JsonNode parent;

    public KeyValuePairList(JsonNode parent){
        super();
        this.parent = parent;
    }

    public JsonNode getParent() {
        return parent;
    }

    public String toString(){
        return "KeyValuePairList("+this.stream().map((KeyValuePair v)->v.getKey()).collect(Collectors.toList())+")";
    }

//    public int hashCode(){
//        return new HashCodeBuilder()
//                .append(getParent())
//                .appendSuper(super.hashCode())
//                .toHashCode();
//    }
//
//    public boolean equals(Object other) {
//        return other instanceof KeyValuePairList
//            && new EqualsBuilder()
//                .append(getParent(),((KeyValuePairList)other).getParent())
//                .appendSuper(super.equals(other))
//                .isEquals();
//    }

    public Object getByKey(K key) {
        for (KeyValuePair<K,V> kv:this){
            if (kv.getKey().equals(key)){
                return kv.getValue();
            }
        }
        return null;
    }

    public void putByKey(K key, V value) {
        for (KeyValuePair<K,V> kv:this){
            if (kv.getKey().equals(key)){
                kv.setValue(value);
                return;
            }
        }
        add(new KeyValuePair<>(this,key,value));
    }

    public KeyValuePair getPairByKey(String key) {
        for (KeyValuePair<K,V> kv:this){
            if (kv.getKey().equals(key)){
                return kv;
            }
        }
        return null;
    }

    public void replace(KeyValuePair<K, V> oldPair, KeyValuePair<K, V> newPair) {
        int index = indexOf(oldPair);
        remove(index);
        add(index, newPair);
    }
}
