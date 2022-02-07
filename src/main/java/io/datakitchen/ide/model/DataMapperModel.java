package io.datakitchen.ide.model;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.vfs.VirtualFile;
import io.datakitchen.ide.editors.neweditors.mapper.ConnectionImpl;
import io.datakitchen.ide.editors.neweditors.mapper.DataMapperConnectionList;

import java.awt.datatransfer.Transferable;
import java.util.Set;

public interface DataMapperModel extends NodeModel{
    String getDescription();
    void setDescription(String description);
    ConnectionList getDataSources();
    ConnectionList getDataSinks();
    Key createKeyFromTransferable(ConnectionList connectionList, Connection connection, Transferable transferable);
    void createSourceFileKey(Connection connection, String name);
    Set<Mapping> getMappings();
    void addDataMapperModelListener(DataMapperModelListener listener);
    void removeDataMapperModelListener(DataMapperModelListener listener);
    Module getModule();

    Key createKeyFromFile(DataMapperConnectionList connectionList, ConnectionImpl connection, VirtualFile newFile);
}
