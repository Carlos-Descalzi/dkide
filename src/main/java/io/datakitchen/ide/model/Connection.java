package io.datakitchen.ide.model;

import com.intellij.openapi.vfs.VirtualFile;

import java.awt.datatransfer.Transferable;
import java.util.Set;

public interface Connection {
    DataType getDataType();
    Connector getConnector();
    String getName();
    void setName(String name);
    Set<Test> getTests();
    void addTest(Test test);
    void updateTest(Test test);
    void removeTest(Test test);
    Set<RuntimeVariable> getVariables();
    void addVariable(RuntimeVariable variable);
    void removeVariable(RuntimeVariable variable);
    Set<Key> getKeys();
    void removeKey(Key key);
    void addConnectionListener(ConnectionListener listener);
    void removeConnectionListener(ConnectionListener listener);
    void addKeyFromTransferable(Transferable transferable);

    void addKeyFromFile(VirtualFile newFile);
    NodeModel getModel();
}
