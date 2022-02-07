package io.datakitchen.ide.editors.neweditors.container;

import com.intellij.openapi.vfs.VirtualFile;
import io.datakitchen.ide.editors.neweditors.FileTest;
import io.datakitchen.ide.model.*;
import io.datakitchen.ide.ui.EventSupport;

import java.awt.datatransfer.Transferable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

public class ConnectionImpl implements Connection {

    private final ContainerConnectionList connectionList;
    private final EventSupport<ConnectionListener> listeners = EventSupport.of(ConnectionListener.class);
    private final Connector connector;
    private final Set<Test> tests = new LinkedHashSet<>();
    private final Set<RuntimeVariable> runtimeVariables = new LinkedHashSet<>();
    private final Set<Key> keys = new LinkedHashSet<>();
    private String name;
    private final ContainerModel model;

    public ConnectionImpl(ContainerModel model, ContainerConnectionList connectionList, Connector connector){
        this.model = model;
        this.connectionList = connectionList;
        this.name = connector.getName();
        this.connector = connector;
    }

    public ConnectionImpl(ContainerModel model, ContainerConnectionList connectionList, String name, Connector connector){
        this.model = model;
        this.connectionList = connectionList;
        this.name = name;
        this.connector = connector;
    }

    @Override
    public DataType getDataType() {
        return connectionList.getDataType(connector.getConnectorType());
    }

    @Override
    public Connector getConnector() {
        return connector;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        String oldName = this.name;
        this.name = name;
        listeners.getProxy().nameChanged(new ConnectionEvent(this, oldName, name));
    }

    @Override
    public Set<Test> getTests() {
        return tests;
    }

    @Override
    public void addTest(Test test) {
        if (tests.add(test)){
            // TODO: merge with datamapper connection

            if (test.getVariable().getVariableName() == null){
                if (test instanceof FileTest){
                    RuntimeVariable variable = findVariableForAttribute(test.getVariable().getAttribute(), ((FileTest) test).getKey());
                    if (variable == null){
                        test.getVariable().setVariableName(makeVariableNameForFileTest((FileTest) test));
                        ((FileTest)test).getKey().addVariable(test.getVariable());
                    } else {
                        test.setVariable(variable);
                    }

                } else {
                    RuntimeVariable variable = findVariableForAttribute(test.getVariable().getAttribute());
                    if (variable == null){
                        test.getVariable().setVariableName(makeVariableNameForTest(test, variable.getAttribute()));
                        addVariable(test.getVariable());
                    } else {
                        test.setVariable(variable);
                    }
                }
            }

            listeners.getProxy().testAdded(new ConnectionEvent(this, test));
        }
    }

    public void updateTest(Test test){
        listeners.getProxy().testChanged(new ConnectionEvent(this, test));
    }

    private String makeVariableNameForFileTest(FileTest fileTest) {
        // TODO: merge with datamapper connection
        return "testvar_"+this.name.toLowerCase()
                .replace(".","_")
                +"_"+fileTest.getKey().getName().toLowerCase()
                .replace(".","_")
                +fileTest.getVariable().getAttribute().getName().toLowerCase();
    }

    private String makeVariableNameForTest(Test test, VariableDescription attribute) {
        // TODO: merge with datamapper connection
        return "testvar_"+this.name.toLowerCase()
                .replace(".","_")
                +attribute.getName().toLowerCase();
    }

    private RuntimeVariable findVariableForAttribute(VariableDescription attribute, Key key) {
        // TODO: merge with datamapper connection
        RuntimeVariable variable = findVariableForAttribute(attribute);
        if (variable != null){
            return variable;
        }
        for (RuntimeVariable var : key.getVariables()){
            if (var.getAttribute().equals(attribute)){
                return var;
            }
        }
        return null;
    }

    private RuntimeVariable findVariableForAttribute(VariableDescription attribute) {
        // TODO: merge with datamapper connection
        for (RuntimeVariable variable: getVariables()){
            if (variable.getAttribute().equals(attribute)){
                return variable;
            }
        }
        return null;
    }

    @Override
    public void removeTest(Test test) {
        if (tests.remove(test)){
            listeners.getProxy().testRemoved(new ConnectionEvent(this, test));
        }
    }

    @Override
    public Set<RuntimeVariable> getVariables() {
        return runtimeVariables;
    }

    @Override
    public void addVariable(RuntimeVariable variable) {
        if (runtimeVariables.add(variable)){
            listeners.getProxy().variableAdded(new ConnectionEvent(this, variable));
        }
    }

    @Override
    public void removeVariable(RuntimeVariable variable) {
        if (runtimeVariables.remove(variable)){
            listeners.getProxy().variableRemoved(new ConnectionEvent(this, variable));
        }
    }

    @Override
    public Set<Key> getKeys() {
        return keys;
    }

    @Override
    public void removeKey(Key key) {

        if (keys.contains(key)) {
            Set<RuntimeVariable> variables = new HashSet<>(key.getVariables());
            for (RuntimeVariable variable : variables) {
                key.removeVariable(variable); // have key notify variables removed and UI updated.
            }
            Set<Test> testsToRemove = new HashSet<>();
            for (Iterator<Test> i = tests.iterator(); i.hasNext(); ) {
                Test test = i.next();
                if (test instanceof FileTest && ((FileTest) test).getKey() == key) {
                    testsToRemove.add(test);
                }
            }
            for (Test test : testsToRemove) {
                removeTest(test); // now same thing with file tests.
            }

            keys.remove(key);
            listeners.getProxy().keyRemoved(new ConnectionEvent(this, key));
        }
    }

    @Override
    public void addConnectionListener(ConnectionListener listener) {
        listeners.addListener(listener);
    }

    @Override
    public void removeConnectionListener(ConnectionListener listener) {
        listeners.removeListener(listener);
    }

    @Override
    public void addKeyFromTransferable(Transferable transferable) {
        Key key = connectionList.getModel().createKeyFromTransferable(connectionList, this, transferable);
        if (key != null){
            addKey(key);
        }
    }

    public void addKey(Key key){
        if (keys.add(key)){
            listeners.getProxy().keyAdded(new ConnectionEvent(this, key));
        }
    }

    @Override
    public void addKeyFromFile(VirtualFile newFile) {
        Key key = connectionList.getModel().createKeyFromFile(connectionList, this, newFile);
        if (key != null){
            addKey(key);
        }
    }

    @Override
    public NodeModel getModel() {
        return model;
    }

}
