package io.datakitchen.ide.model;

import java.util.EventListener;

public interface ConnectionListener extends EventListener {
    void nameChanged(ConnectionEvent event);
    void testAdded(ConnectionEvent event);
    void testChanged(ConnectionEvent event);
    void testRemoved(ConnectionEvent event);
    void variableAdded(ConnectionEvent event);
    void variableRemoved(ConnectionEvent event);
    void keyAdded(ConnectionEvent event);
    void keyRemoved(ConnectionEvent event);
}
