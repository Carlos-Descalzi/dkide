package io.datakitchen.ide.model;

import java.util.EventListener;

public interface ConnectionListListener extends EventListener {
    void connectionAdded(ConnectionListEvent event);
    void connectionRemoved(ConnectionListEvent event);
}
