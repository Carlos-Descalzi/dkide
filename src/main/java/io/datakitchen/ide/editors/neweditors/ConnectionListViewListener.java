package io.datakitchen.ide.editors.neweditors;

import java.util.EventListener;

public interface ConnectionListViewListener extends EventListener {
    default void connectionViewAdded(ConnectionListViewEvent e){}
    default void connectionViewRemoved(ConnectionListViewEvent e){}
}
