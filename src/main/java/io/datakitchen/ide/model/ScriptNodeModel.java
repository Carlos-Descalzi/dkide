package io.datakitchen.ide.model;

import java.util.List;

public interface ScriptNodeModel extends ContainerModel{
    void addScriptNodeModelListener(ScriptNodeModelListener listener);
    void removeScriptNodeModelListener(ScriptNodeModelListener listener);
    List<ScriptNodeKey> getKeys();
    void addKey(ScriptNodeKey key);
    void removeKey(ScriptNodeKey key);

    List<String> getPipDependencies();

    void setPipDependencies(List<String> pipDependencies);

    List<String> getAptDependencies();

    void setAptDependencies(List<String> aptDependencies);

}
