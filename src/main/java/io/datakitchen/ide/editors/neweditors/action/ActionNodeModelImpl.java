package io.datakitchen.ide.editors.neweditors.action;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.vfs.VirtualFile;
import io.datakitchen.ide.editors.neweditors.AbstractNodeModel;
import io.datakitchen.ide.model.*;
import io.datakitchen.ide.ui.EventSupport;
import io.datakitchen.ide.util.ObjectUtil;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ActionNodeModelImpl extends AbstractNodeModel implements ActionNodeModel{

    private final List<Connection> connections = new ArrayList<>();
    private final EventSupport<ConnectionListListener> connectionListeners = EventSupport.of(ConnectionListListener.class);

    private final ActionConnectionList connectionList = new ActionConnectionList() {
        @Override
        public ActionNodeModel getModel() {
            return ActionNodeModelImpl.this;
        }

        @Override
        public DataType getDataType(ConnectorType connectorType) {
            return DataSourceType.forConnectorType(connectorType);
        }

        @Override
        public void addConnection(ConnectionImpl connection) {
            connections.add(connection);
        }

        @Override
        public List<Connection> getConnections() {
            return connections;
        }

        @Override
        public void addConnectionForConnector(Connector connector) {
            ConnectionImpl connection = new ConnectionImpl(ActionNodeModelImpl.this, this, connector);
            connections.add(connection);
            connectionListeners.getProxy().connectionAdded(new ConnectionListEvent(this,connection));
        }

        @Override
        public void addConnectionListListener(ConnectionListListener listener) {
            connectionListeners.addListener(listener);
        }

        @Override
        public void removeConnectionListListener(ConnectionListListener listener) {
            connectionListeners.removeListener(listener);
        }

        @Override
        public void removeConnection(Connection connection) {
            if (connections.remove(connection)){
                connectionListeners.getProxy().connectionRemoved(new ConnectionListEvent(this, connection));
            }
        }
    };

    public ActionNodeModelImpl(Module module, VirtualFile nodeFolder){
        super(module, nodeFolder);
    }

    @Override
    public ConnectionList getConnectionList() {
        return connectionList;
    }

    @Override
    public Key createKeyFromTransferable(ActionConnectionList connectionList, ConnectionImpl connection, Transferable transferable) {
        if (transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)){
            try {
                List<File> files = ObjectUtil.cast(transferable.getTransferData(DataFlavor.javaFileListFlavor));
                for (File file: files){
                    if (file.getName().endsWith(".sql")){
                        return new ActionKey(connection, file.getName());
                    }
                }
            }catch (Exception ex){
                ex.printStackTrace();
            }
        }
        return null;
    }

    @Override
    public Key createKeyFromFile(ActionConnectionList connectionList, ConnectionImpl connection, VirtualFile newFile) {
        return new ActionKey(connection, newFile.getName());
    }

    @Override
    public void notifySourceKeyRemoved(Key key) {

    }


}
