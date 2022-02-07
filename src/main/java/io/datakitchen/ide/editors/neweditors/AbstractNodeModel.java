package io.datakitchen.ide.editors.neweditors;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.vfs.VirtualFile;
import io.datakitchen.ide.model.*;
import io.datakitchen.ide.ui.EventSupport;

import java.util.LinkedHashSet;
import java.util.Set;

public abstract class AbstractNodeModel implements NodeModel {

    private final VirtualFile nodeFolder;
    private final Module module;
    private final EventSupport<NodeModelListener> listeners = EventSupport.of(NodeModelListener.class);
    private final Set<Test> tests = new LinkedHashSet<>();
    private String description;

    protected AbstractNodeModel(Module module, VirtualFile nodeFolder){
        this.module = module;
        this.nodeFolder = nodeFolder;
    }

    @Override
    public void addNodeModelListener(NodeModelListener listener) {
        listeners.addListener(listener);
    }

    @Override
    public void removeNodeModelListener(NodeModelListener listener) {
        listeners.removeListener(listener);
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void setDescription(String description) {
        String oldDescription = this.description;
        this.description = description;
        notifyPropertyChanged("description", oldDescription, description);
    }

    protected void notifyPropertyChanged(String propertyName, Object oldValue, Object newValue){
        listeners.getProxy().nodePropertyChanged(new NodeModelEvent(this, propertyName, oldValue, newValue));
    }

    @Override
    public Set<Test> getTests() {
        return tests;
    }

    @Override
    public void addTest(Test test) {
        if (tests.add(test)){
            listeners.getProxy().testAdded(new NodeModelEvent(this, test));
        }
    }

    @Override
    public void removeTest(Test test) {
        if (tests.remove(test)){
            listeners.getProxy().testRemoved(new NodeModelEvent(this, test));
        }
    }

    @Override
    public Module getModule() {
        return module;
    }

    @Override
    public String getNodeName() {
        return nodeFolder.getName();
    }
}
