package io.datakitchen.ide.editors.neweditors.container;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.vfs.VirtualFile;
import io.datakitchen.ide.model.ScriptNodeKey;
import io.datakitchen.ide.model.ScriptNodeModel;
import io.datakitchen.ide.model.ScriptNodeModelEvent;
import io.datakitchen.ide.model.ScriptNodeModelListener;
import io.datakitchen.ide.ui.EventSupport;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

public class ScriptNodeModelImpl extends ContainerModelImpl implements ScriptNodeModel, PropertyChangeListener {

    private final EventSupport<ScriptNodeModelListener> listeners = EventSupport.of(ScriptNodeModelListener.class);
    private final List<ScriptNodeKey> keys = new ArrayList<>();
    private List<String> pipDependencies = new ArrayList<>();
    private List<String> aptDependencies = new ArrayList<>();

    public ScriptNodeModelImpl(Module module, VirtualFile notebookFile) {
        super(module, notebookFile);
    }

    @Override
    public void addScriptNodeModelListener(ScriptNodeModelListener listener) {
        listeners.addListener(listener);
    }

    @Override
    public void removeScriptNodeModelListener(ScriptNodeModelListener listener) {
        listeners.removeListener(listener);
    }

    @Override
    public List<ScriptNodeKey> getKeys() {
        return keys;
    }

    @Override
    public void addKey(ScriptNodeKey key) {
        if (keys.add(key)){
            key.addPropertyChangeListener(this);
            listeners.getProxy().keyAdded(new ScriptNodeModelEvent(this, key));
        }
    }

    @Override
    public void removeKey(ScriptNodeKey key) {
        if (keys.remove(key)){
            key.removePropertyChangeListener(this);
            listeners.getProxy().keyRemoved(new ScriptNodeModelEvent(this, key));
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        listeners.getProxy().keyChanged(new ScriptNodeModelEvent(this, (ScriptNodeKey) evt.getSource()));
    }


    public List<String> getPipDependencies() {
        return pipDependencies;
    }

    public void setPipDependencies(List<String> pipDependencies) {
        List<String> oldValue = this.pipDependencies;
        this.pipDependencies = pipDependencies;
        listeners.getProxy().propertyChanged(new ScriptNodeModelEvent(this));
    }

    public List<String> getAptDependencies() {
        return aptDependencies;
    }

    public void setAptDependencies(List<String> aptDependencies) {
        List<String> oldValue = this.aptDependencies;
        this.aptDependencies = aptDependencies;
        listeners.getProxy().propertyChanged(new ScriptNodeModelEvent(this));
    }

    @Override
    public void renameFile(VirtualFile file, String newName) {
        String oldName = file.getName();
        super.renameFile(file, newName);
        for (ScriptNodeKey key: keys){
            if (key.getScript().equals(oldName)){
                key.setScript(newName);
                listeners.getProxy().keyChanged(new ScriptNodeModelEvent(this, key));
            }
        }
    }
}
