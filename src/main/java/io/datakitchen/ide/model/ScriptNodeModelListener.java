package io.datakitchen.ide.model;

import java.util.EventListener;

public interface ScriptNodeModelListener extends EventListener {
    void keyAdded(ScriptNodeModelEvent event);
    void keyRemoved(ScriptNodeModelEvent event);
    void keyChanged(ScriptNodeModelEvent scriptNodeModelEvent);
    void propertyChanged(ScriptNodeModelEvent scriptNodeModelEvent);
}
