package io.datakitchen.ide.model;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.vfs.VirtualFile;
import io.datakitchen.ide.editors.neweditors.action.ActionConnectionList;
import io.datakitchen.ide.editors.neweditors.action.ConnectionImpl;

import java.awt.datatransfer.Transferable;

public interface ActionNodeModel extends NodeModel{

    ConnectionList getConnectionList();

    Key createKeyFromTransferable(ActionConnectionList connectionList, ConnectionImpl connection, Transferable transferable);

    Module getModule();

    Key createKeyFromFile(ActionConnectionList connectionList, ConnectionImpl connection, VirtualFile newFile);

    void notifySourceKeyRemoved(Key key);
}
