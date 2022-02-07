package io.datakitchen.ide.model;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import io.datakitchen.ide.editors.neweditors.container.ConnectionImpl;
import io.datakitchen.ide.editors.neweditors.container.ContainerConnectionList;

import java.awt.datatransfer.Transferable;
import java.io.File;
import java.util.Set;

public interface ContainerModel extends NodeModel{
    ConnectionList getDataSources();
    ConnectionList getDataSinks();
    Key createKeyFromTransferable(ConnectionList connectionList, Connection connection, Transferable transferable);
    void createSourceFileKey(Connection connection, String name);
    Set<VirtualFile> getFiles();
    void addFile(VirtualFile file);
    void addFile(File file);
    void addNewFile(String fileName);
    void removeFile(VirtualFile file);

    Set<VirtualFile> getTestFiles();
    void addTestFile(VirtualFile file);
    void addTestFile(File file);
    void addNewTestFile(String fileName);
    void removeTestFile(VirtualFile file);

    Set<Assignment> getAssignments();
    void addAssignment(Assignment assignment);
    void removeAssignment(Assignment assignment);
    void addContainerModelListener(ContainerModelListener listener);
    void removeContainerModelListener(ContainerModelListener listener);
    String getImageName();
    void setImageName(String imageName);
    String getUsername();
    void setUsername(String username);
    String getPassword();
    void setPassword(String password);
    String getRegistry();
    void setRegistry(String registry);
    String getCommand();
    void setCommand(String command);
    String getDescription();
    void setDescription(String description);
    Project getProject();

    Module getModule();

    Key createKeyFromFile(ContainerConnectionList connectionList, ConnectionImpl connection, VirtualFile newFile);

    VirtualFile getFile(String script);

    void renameFile(VirtualFile file, String newName);
}
