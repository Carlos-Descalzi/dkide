package io.datakitchen.ide.editors.neweditors.container;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.ThrowableComputable;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import io.datakitchen.ide.model.*;
import io.datakitchen.ide.ui.EventSupport;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class ContainerModelImpl implements ContainerModel {

    private final Module module;
    private final VirtualFile notebookFile;
    private final VirtualFile dockerShareFolder;
    private final VirtualFile testFilesFolder;
    private String description;
    private String imageName;
    private String username;
    private String password;
    private String registry;
    private String command;

    private final EventSupport<NodeModelListener> nodeModelListeners = EventSupport.of(NodeModelListener.class);

    private Set<Test> tests = new LinkedHashSet<>();

    private final Set<Assignment> assignments = new LinkedHashSet<>();

    private final EventSupport<ConnectionListListener> dataSourceListeners = EventSupport.of(ConnectionListListener.class);
    private final List<Connection> dataSources = new ArrayList<>();

    private final EventSupport<ConnectionListListener> dataSinkListeners = EventSupport.of(ConnectionListListener.class);
    private final List<Connection> dataSinks = new ArrayList<>();

    private final EventSupport<ContainerModelListener> listeners = EventSupport.of(ContainerModelListener.class);

    private final ContainerConnectionList dataSourceConnectionList = new ContainerConnectionList() {
        @Override
        public ContainerModel getModel() {
            return ContainerModelImpl.this;
        }

        @Override
        public void addConnection(ConnectionImpl connection) {
            dataSources.add(connection);
        }

        @Override
        public DataType getDataType(ConnectorType connectorType) {
            return DataSourceType.forConnectorType(connectorType);
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
    private final ContainerConnectionList dataSinksConnectionList = new ContainerConnectionList() {
        @Override
        public ContainerModel getModel() {
            return ContainerModelImpl.this;
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

    public ContainerModelImpl(Module module, VirtualFile notebookFile){
        this.module = module;
        this.notebookFile = notebookFile;
        this.dockerShareFolder = notebookFile.getParent().findChild("docker-share");
        this.testFilesFolder = getTestFilesFolder(notebookFile);

    }

    private VirtualFile getTestFilesFolder(VirtualFile notebookFile){
        VirtualFile testFilesFolder = notebookFile.getParent().findChild("test-files");
        if (testFilesFolder == null){
            try {
                ThrowableComputable<VirtualFile, IOException> action = () ->
                        notebookFile.getParent().createChildDirectory(ContainerModelImpl.this, "test-files");
                testFilesFolder = ApplicationManager.getApplication().runWriteAction(action);
            }catch(Exception ex){
                throw new RuntimeException(ex);
            }
        }
        return testFilesFolder;
    }

    @Override
    public ConnectionList getDataSources() {
        return dataSourceConnectionList;
    }

    @Override
    public ConnectionList getDataSinks() {
        return dataSinksConnectionList;
    }

    @Override
    public Key createKeyFromTransferable(ConnectionList connectionList, Connection connection, Transferable transferable) {
        if (connectionList == dataSourceConnectionList){
            if (transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                ConnectorNature nature = connection.getConnector().getConnectorType().getNature();
                if (nature == ConnectorNature.SQL) {
                    try {
                        for (File file : (List<File>) transferable.getTransferData(DataFlavor.javaFileListFlavor)) {
                            if (file.getName().endsWith(".sql")) {
                                return new DataSourceSqlKey(
                                    (ConnectionImpl) connection,
                                        file.getName().replace(".","-").replace("/","-"),
                                        file.getName()
                                );
                            }
                        }
                    }catch (Exception ex){
                        ex.printStackTrace();
                    }
                }
            }
        } else {

        }
        return null;
    }

    @Override
    public Key createKeyFromFile(ContainerConnectionList connectionList, ConnectionImpl connection, VirtualFile file) {
        return new DataSourceSqlKey(connection,file.getName().replace(".","-").replace("/","-"), file.getName());
    }

    @Override
    public void createSourceFileKey(Connection connection, String name) {

    }

    @Override
    public Set<VirtualFile> getFiles() {
        return new LinkedHashSet<>(Arrays.asList(dockerShareFolder.getChildren()));
    }

    @Override
    public VirtualFile getFile(String script) {
        return dockerShareFolder.findFileByRelativePath(script);
    }

    @Override
    public void renameFile(VirtualFile file, String newName) {
        ApplicationManager.getApplication().runWriteAction(()->{
            try {
                String oldName = file.getName();
                file.rename(ContainerModelImpl.this, newName);
                listeners.getProxy().inputFileRenamed(new ContainerModelEvent(this, file, oldName));
            }catch(Exception ex){
                ex.printStackTrace();
            }
        });
    }

    @Override
    public void addNewFile(String fileName) {
        ApplicationManager.getApplication().runWriteAction(()->{
            try {
                VirtualFile newFile = dockerShareFolder.createChildData(this, fileName);
                listeners.getProxy().inputFilesAdded(new ContainerModelEvent(this, List.of(newFile), true, false));
            }catch(Exception ex){
                ex.printStackTrace();
            }
        });
    }

    @Override
    public void addFile(VirtualFile file) {
        ApplicationManager.getApplication().runWriteAction(()->{
            try {
                VirtualFile newFile = VfsUtil.copyFile(ContainerModelImpl.this, file, dockerShareFolder);
                listeners.getProxy().inputFilesAdded(new ContainerModelEvent(this, List.of(newFile), false, false));
            }catch (Exception ex){
                ex.printStackTrace();
            }
        });
    }

    @Override
    public void addFile(File file) {
        VirtualFile vFile = VfsUtil.findFileByIoFile(file,true);
        if (vFile != null){
            addFile(vFile);
        }
    }

    @Override
    public void removeFile(VirtualFile file) {
        ApplicationManager.getApplication().runWriteAction(()->{
            try {
                file.delete(ContainerModelImpl.this);
                listeners.getProxy().inputFilesRemoved(new ContainerModelEvent(this, List.of(file), false));
            }catch(Exception ex){
                ex.printStackTrace();
            }
        });
    }

    @Override
    public Set<VirtualFile> getTestFiles() {
        return new LinkedHashSet<>(Arrays.asList(testFilesFolder.getChildren()));
    }

    @Override
    public void addTestFile(VirtualFile file) {
        ApplicationManager.getApplication().runWriteAction(()->{
            try {
                VirtualFile newFile = VfsUtil.copyFile(ContainerModelImpl.this, file, testFilesFolder);
                listeners.getProxy().inputFilesAdded(new ContainerModelEvent(this, List.of(newFile), false,true));
            }catch (Exception ex){
                ex.printStackTrace();
            }
        });
    }

    @Override
    public void addTestFile(File file) {
        VirtualFile vFile = VfsUtil.findFileByIoFile(file, true);
        if (vFile != null){
            addTestFile(vFile);
        }
    }

    @Override
    public void addNewTestFile(String fileName) {
        ApplicationManager.getApplication().runWriteAction(()->{
            try {
                VirtualFile newFile = dockerShareFolder.createChildData(this, fileName);
                listeners.getProxy().inputFilesAdded(new ContainerModelEvent(this, List.of(newFile), true, true));
            }catch(Exception ex){
                ex.printStackTrace();
            }
        });
    }

    @Override
    public void removeTestFile(VirtualFile file) {
        ApplicationManager.getApplication().runWriteAction(()->{
            try {
                file.delete(ContainerModelImpl.this);
                listeners.getProxy().inputFilesRemoved(new ContainerModelEvent(this, List.of(file), true));
            }catch(Exception ex){
                ex.printStackTrace();
            }
        });
    }

    @Override
    public Set<Assignment> getAssignments() {
        return assignments;
    }

    @Override
    public void addAssignment(Assignment assignment) {
        if (assignments.add(assignment)){
            listeners.getProxy().variableAssignmentAdded(new ContainerModelEvent(this, assignment));
        }
    }

    @Override
    public void removeAssignment(Assignment assignment) {
        if (assignments.remove(assignment)){
            listeners.getProxy().variableAssignmentRemoved(new ContainerModelEvent(this, assignment));
        }
    }

    @Override
    public void addContainerModelListener(ContainerModelListener listener) {
        listeners.addListener(listener);
    }

    @Override
    public void removeContainerModelListener(ContainerModelListener listener) {
        listeners.removeListener(listener);
    }

    @Override
    public Project getProject() {
        return module.getProject();
    }

    @Override
    public Module getModule() {
        return module;
    }

    @Override
    public String getNodeName() {
        return notebookFile.getParent().getName();
    }

    private void doAddDataSource(Connector connector) {
        Connection connection = new ConnectionImpl(this, dataSourceConnectionList, makeDsName(connector, dataSources), connector);
        dataSources.add(connection);
        dataSourceListeners.getProxy().connectionAdded(new ConnectionListEvent(this, connection));
    }

    private void removeSourceConnection(Connection connection) {
        if (dataSources.remove(connection)){
            dataSourceListeners.getProxy().connectionRemoved(new ConnectionListEvent(this, connection));
        }
    }

    private void doAddDataSink(Connector connector) {
        Connection connection = new ConnectionImpl(this, dataSinksConnectionList, makeDsName(connector, dataSinks), connector);
        dataSinks.add(connection);
        dataSinkListeners.getProxy().connectionAdded(new ConnectionListEvent(this, connection));
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

    private void removeSinkConnection(Connection connection) {
        if (dataSinks.remove(connection)){
            dataSinkListeners.getProxy().connectionRemoved(new ConnectionListEvent(this, connection));
        }
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        String oldImageName = this.imageName;
        this.imageName = imageName;
        listeners.getProxy().nodePropertyChanged(new ContainerModelEvent(this, "imageName",oldImageName, imageName));
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        String oldUsername = this.username;
        this.username = username;
        listeners.getProxy().nodePropertyChanged(new ContainerModelEvent(this,"username",oldUsername, username));
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        String oldPassword = this.password;
        this.password = password;
        listeners.getProxy().nodePropertyChanged(new ContainerModelEvent(this, "password",oldPassword, password));
    }

    public String getRegistry() {
        return registry;
    }

    public void setRegistry(String registry) {
        String oldRegistry = this.registry;
        this.registry = registry;
        listeners.getProxy().nodePropertyChanged(new ContainerModelEvent(this, "registry",oldRegistry, registry));
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        String oldCommand = this.command;
        this.command = command;
        listeners.getProxy().nodePropertyChanged(new ContainerModelEvent(this, "command",oldCommand, command));
    }

    @Override
    public void addNodeModelListener(NodeModelListener listener) {
        nodeModelListeners.addListener(listener);
    }

    @Override
    public void removeNodeModelListener(NodeModelListener listener) {
        nodeModelListeners.removeListener(listener);
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void setDescription(String description) {
        String oldDescription = this.description;
        this.description = description;
        listeners.getProxy().nodePropertyChanged(new ContainerModelEvent(this, "description", oldDescription, description));
    }

    @Override
    public Set<Test> getTests() {
        return tests;
    }

    @Override
    public void addTest(Test test) {
        if (tests.add(test)){
            nodeModelListeners.getProxy().testAdded(new NodeModelEvent(this, test));
        }
    }

    @Override
    public void removeTest(Test test) {
        if (tests.remove(test)){
            nodeModelListeners.getProxy().testRemoved(new NodeModelEvent(this, test));
        }
    }

}
