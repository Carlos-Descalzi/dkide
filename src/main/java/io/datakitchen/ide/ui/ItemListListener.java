package io.datakitchen.ide.ui;

import java.util.EventListener;

public interface ItemListListener extends EventListener {
    void itemAdded(ItemListEvent event);
    void itemRemoved(ItemListEvent event);
    void itemChanged(ItemListEvent event);
}
