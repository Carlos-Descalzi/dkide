package io.datakitchen.ide.ui;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class ListListModel<T> extends AbstractListModel<T> {

    private List<T> items;

    public ListListModel() {
        this(new ArrayList<>());
    }

    public ListListModel(List<T> items) {
        this.items = items;
    }

    public void set(List<T> items){
        this.items = items;
        fireContentsChanged(this,0,items.size());
    }

    @Override
    public int getSize() {
        return items.size();
    }

    @Override
    public T getElementAt(int index) {
        return items.get(index);
    }

    public void remove(int index) {
        items.remove(index);
        fireIntervalRemoved(this,index,index);
    }

    public void add(T object) {
        int index = items.size();
        items.add(object);
        fireIntervalAdded(this,index,index);
    }

    public List<T> getList() {
        return items;
    }

    public void decreasePosition(int index) {
        if (index > 0) {
            T item = items.remove(index);
            items.add(index, item);
            fireContentsChanged(this,0,items.size()-1);
        }
    }

    public void increasePosition(int index) {
        if (index < items.size()-1){
            T item = items.remove(index);
            items.add(index-1,item);
            fireContentsChanged(this,0,items.size()-1);
        }
    }
}
