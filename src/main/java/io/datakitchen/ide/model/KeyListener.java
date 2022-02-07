package io.datakitchen.ide.model;

import java.util.EventListener;

public interface KeyListener extends EventListener {
    void variableAdded(KeyEvent event);
    void variableRemoved(KeyEvent event);
    void keyChanged(KeyEvent event);
}
