package io.datakitchen.ide.editors.neweditors.mapper;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.vfs.VirtualFile;
import io.datakitchen.ide.editors.neweditors.KeyReference;
import io.datakitchen.ide.model.DataMapperModel;
import io.datakitchen.ide.model.DataMapperModelListener;
import io.datakitchen.ide.model.*;
import io.datakitchen.ide.model.Mapping;
import io.datakitchen.ide.ui.EventSupport;
import io.datakitchen.ide.util.RecipeUtil;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class DataMapperModelImpl implements DataMapperModel {

    private final Module module;
    private final EventSupport<DataMapperModelListener> listeners = EventSupport.of(DataMapperModelListener.class);

    private final EventSupport<ConnectionListListener> dataSourceListeners = EventSupport.of(ConnectionListListener.class);
    private final List<Connection> dataSources = new ArrayList<>();

    private final EventSupport<ConnectionListListener> dataSinkListeners = EventSupport.of(ConnectionListListener.class);
    private final List<Connection> dataSinks = new ArrayList<>();
    private String description;

    private final DataMapperConnectionList dataSourceConnectionList = new DataMapperConnectionList() {
        @Override
        public DataMapperModel getModel() {
            return DataMapperModelImpl.this;
        }

        @Override
        public DataType getDataType(ConnectorType connectorType) {
            return DataSourceType.forConnectorType(connectorType);
        }

        @Override
        public void addConnection(ConnectionImpl connection) {
            dataSources.add(connection);
        }

        @Override
        public List<Connection> getConnections() {
            return dataSources;
        }

        @Override
        public void addConnectionForConnector(Connector connector) {
            doAddDataSource(connector);
        }

        @Override
        public void addConnectionListListener(ConnectionListListener listener) {
            dataSourceListeners.addListener(listener);
        }

        @Override
        public void removeConnectionListListener(ConnectionListListener listener) {
            dataSourceListeners.removeListener(listener);
        }

        @Override
        public void removeConnection(Connection connection) {
            removeSourceConnection(connection);
        }
    };

    private final DataMapperConnectionList dataSinkConnectionList = new DataMapperConnectionList() {
        @Override
        public DataMapperModel getModel() {
            return DataMapperModelImpl.this;
        }

        @Override
        public void addConnection(ConnectionImpl connection) {
            dataSinks.add(connection);
        }

        @Override
        public DataType getDataType(ConnectorType connectorType) {
            return DataSinkType.forConnectorType(connectorType);
        }

        @Override
        public List<Connection> getConnections() {
            return dataSinks;
        }

        @Override
        public void addConnectionForConnector(Connector connector) {
            doAddDataSink(connector);
        }

        @Override
        public void addConnectionListListener(ConnectionListListener listener) {
            dataSinkListeners.addListener(listener);
        }

        @Override
        public void removeConnectionListListener(ConnectionListListener listener) {
            dataSinkListeners.removeListener(listener);
        }

        @Override
        public void removeConnection(Connection connection) {
            removeSinkConnection(connection);
        }
    };

    public DataMapperModelImpl(Module module){
        this.module = module;
    }

    @Override
    public void addNodeModelListener(NodeModelListener listener) {

    }

    @Override
    public void removeNodeModelListener(NodeModelListener listener) {

    }

    public String getDescription(){
        return description;
    }

    public void setDescription(String description){
        String oldDescription = this.description;
        this.description = description;
        listeners.getProxy().nodePropertyChanged(new DataMapperModelEvent(this, "description", oldDescription, description));
    }

    @Override
    public Set<Test> getTests() {
        return Set.of();
    }

    @Override
    public void addTest(Test test) {

    }

    @Override
    public void removeTest(Test test) {

    }

    @Override
    public ConnectionList getDataSources() {
        return dataSourceConnectionList;
    }

    @Override
    public ConnectionList getDataSinks() {
        return dataSinkConnectionList;
    }

    private void doAddDataSource(Connector connector) {
        if (DataSourceType.forConnectorType(connector.getConnectorType()) != null) {
            Connection connection = new ConnectionImpl(this,dataSourceConnectionList, makeDsName(connector, dataSources), connector);
            dataSources.add(connection);
            dataSourceListeners.getProxy().connectionAdded(new ConnectionListEvent(this, connection));
        }
    }

    private void doAddDataSink(Connector connector) {
        if (DataSinkType.forConnectorType(connector.getConnectorType()) != null) {
            Connection connection = new ConnectionImpl(this, dataSinkConnectionList, makeDsName(connector, dataSinks), connector);
            dataSinks.add(connection);
            dataSinkListeners.getProxy().connectionAdded(new ConnectionListEvent(this, connection));
        }
    }

    private String makeDsName(Connector connector, List<Connection> connections) {
        String connectorName = connector.getName();
        String name = connectorName;
        int i = 2;
        while (nameExists(name, connections)){
            name = connectorName+"-"+(i++);
        }
        return name;
    }

    private boolean nameExists(String name, List<Connection> connections) {
        return connections.stream().anyMatch(c -> c.getName().equals(name));
    }

    @Override
    public void addDataMapperModelListener(DataMapperModelListener listener) {
        listeners.addListener(listener);
    }

    @Override
    public void removeDataMapperModelListener(DataMapperModelListener listener) {
        listeners.removeListener(listener);
    }

    @Override
    public Module getModule() {
        return module;
    }

    @Override
    public String getNodeName() {
        return null;
    }

    @Override
    public Key createKeyFromFile(DataMapperConnectionList connectionList, ConnectionImpl connection, VirtualFile newFile) {
        return makeSourceKey(connection, newFile);
    }

    @Override
    public Key createKeyFromTransferable(ConnectionList connectionList, Connection connection, Transferable transferable) {
        if (connectionList == dataSourceConnectionList) {
            if (transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                try {

                    List<File> files = (List<File>) transferable.getTransferData(DataFlavor.javaFileListFlavor);

                    for (File file : files) {
                        Key key = makeSourceKey(connection,file);
                        if (key != null){
                            return key;
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        } else {
            if (transferable.isDataFlavorSupported(KeyReference.FLAVOR)){
                try {
                    KeyReference keyReference = (KeyReference) transferable.getTransferData(KeyReference.FLAVOR);
                    addSinkMapping(connection, keyReference);
                }catch (Exception ex){
                    ex.printStackTrace();
                }

            }

        }
        return null;
    }

    @Override
    public void createSourceFileKey(Connection connection, String name) {
        Key key = new DataSourceFileKey(
            (ConnectionImpl) connection,
            name.replace("/","-").replace(".","-"),
            name
        );
        ((ConnectionImpl)connection).addKey(key);
    }

    private void addSinkMapping(Connection connection, KeyReference keyReference) {
        dataSources.stream()
            .filter(c -> c.getName().equals(keyReference.getConnectionName()))
            .findFirst().ifPresent(c->{
                c.getKeys().stream().filter(k ->k.getName().equals(keyReference.getKeyName()))
                    .findFirst().ifPresent( k -> {
                        Key sinkKey = makeSinkKey(c,k, connection);
                        if (sinkKey != null) {
                            ((ConnectionImpl) connection).addKey(sinkKey);
                        }
                        listeners.getProxy().mappingsAdded(
                            new DataMapperModelEvent(
                                DataMapperModelImpl.this,
                                    List.of(new MappingImpl(k, sinkKey))
                            )
                        );
                    });
            });

    }

    private Key makeSinkKey(Connection sourceConnection, Key sourceKey, Connection sinkConnection) {

        ConnectorNature sourceNature = sourceConnection.getConnector().getConnectorType().getNature();
        ConnectorNature sinkNature = sinkConnection.getConnector().getConnectorType().getNature();

        String keyName = sourceKey.getName();
        if (sinkNature == ConnectorNature.FILE){
            return new DataSinkFileKey((ConnectionImpl) sinkConnection, keyName, sourceKey);
        } else if (sinkNature == ConnectorNature.SQL){
            String tableName = sourceKey.getName();
            if (tableName.contains(".")) {
                tableName = tableName.substring(0, tableName.indexOf('.'));
            }
            return new DataSinkSqlKey((ConnectionImpl) sinkConnection, keyName, sourceKey, tableName);
        }
        return null;
    }

    public void notifySourceKeyRemoved(Key sourceKey) {
        for (Connection sink: dataSinks){
            Set<Key> toRemove = new HashSet<>();
            for (Key key: sink.getKeys()){
                SinkKey sinkKey = (SinkKey) key;
                if (sinkKey.getSourceKey().equals(sourceKey)){
                    listeners.getProxy().mappingsRemoved(new DataMapperModelEvent(this,
                        List.of(new MappingImpl(
                                sourceKey,
                                sinkKey
                        ))));
                    toRemove.add(sinkKey);
                }
            }
            for (Key key:toRemove){
                sink.removeKey(key);
            }
        }
    }

    private static class MappingImpl implements Mapping{
        private final Key sourceKey;
        private final Key sinkKey;

        public MappingImpl(Key sourceKey, Key sinkKey) {
            this.sourceKey = sourceKey;
            this.sinkKey = sinkKey;
        }

        @Override
        public Key getSourceKey() {
            return sourceKey;
        }

        @Override
        public Key getSinkKey() {
            return sinkKey;
        }
    }

    @Override
    public Set<Mapping> getMappings() {
        Set<Mapping> mappings = new LinkedHashSet<>();
        for (Connection sinkConnection:this.dataSinks){
            for (Key key:sinkConnection.getKeys()){
                SinkKey sinkKey = (SinkKey) key;
                mappings.add(new MappingImpl(sinkKey.getSourceKey(), sinkKey));
            }
        }
        return mappings;
    }

    public Key makeSourceKey(Connection connection, File file) throws IOException{
        ConnectorNature nature = connection.getConnector().getConnectorType().getNature();
        String keyName = file.getName().replace("/","-").replace(".","-");
        if (nature == ConnectorNature.FILE) {
            return new DataSourceFileKey((ConnectionImpl) connection, keyName, file.getName());
        } else if (nature == ConnectorNature.SQL){
            if (!file.getName().endsWith(".sql")){
                return null;
            }
            copyFileIfNeeded(file);
            return new DataSourceSqlKey((ConnectionImpl) connection, keyName, file.getName());
        }
        return null;
    }
    public Key makeSourceKey(Connection connection, VirtualFile file){
        ConnectorNature nature = connection.getConnector().getConnectorType().getNature();
        String keyName = file.getName().replace("/","-").replace(".","-");
        if (nature == ConnectorNature.FILE) {
            return new DataSourceFileKey((ConnectionImpl) connection, keyName, file.getName());
        } else if (nature == ConnectorNature.SQL){
            if (!file.getName().endsWith(".sql")){
                return null;
            }
            return new DataSourceSqlKey((ConnectionImpl) connection, keyName, file.getName());
        }
        return null;
    }

    private void copyFileIfNeeded(File file) throws IOException {
        if (!RecipeUtil.isFileInRecipe(module, file)) {
            RecipeUtil.copyToResourcesFolder(module, file);
        }
    }

    private void removeSourceConnection(Connection connection) {
        Set<Mapping> removedMappings = new LinkedHashSet<>();
        if (dataSources.remove(connection)){
            for (Key key: connection.getKeys()){
                for (Connection sinkConnection:dataSinks){
                    Set<Key> removedSinkKeys = new HashSet<>();
                    for (Key sinkKey : sinkConnection.getKeys()){
                        SinkKey sinkKeyObj = (SinkKey) sinkKey;
                        if (key.equals(sinkKeyObj.getSourceKey())){
                            removedSinkKeys.add(sinkKeyObj);
                            removedMappings.add(new MappingImpl(key, sinkKey));
                        }
                    }
                    for (Key removedKey: removedSinkKeys) {
                        sinkConnection.removeKey(removedKey);
                    }
                }
            }
            listeners.getProxy().mappingsRemoved(new DataMapperModelEvent(this, removedMappings));
            dataSourceListeners.getProxy().connectionRemoved(new ConnectionListEvent(this, connection));
        }
    }

    private void removeSinkConnection(Connection connection) {
        if (dataSinks.remove(connection)){
            Set<Mapping> removedMappings = new HashSet<>();
            for (Key key:connection.getKeys()){
                SinkKey sinkKey = (SinkKey) key;
                Key sourceKey = sinkKey.getSourceKey();
                removedMappings.add(new MappingImpl(sourceKey, sinkKey));
            }
            listeners.getProxy().mappingsRemoved(new DataMapperModelEvent(this, removedMappings));
            dataSinkListeners.getProxy().connectionRemoved(new ConnectionListEvent(this, connection));
        }
    }

}
