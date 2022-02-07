package io.datakitchen.ide.editors.neweditors;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import io.datakitchen.ide.editors.neweditors.container.*;
import io.datakitchen.ide.editors.neweditors.palette.ModuleComponentSource;
import io.datakitchen.ide.model.*;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class ContainerEditor extends BaseSimpleEditor implements RunRequestHandler {

    private ContainerModelImpl model;
    private ModelWriteHandler writeHandler;

    public ContainerEditor(Module module, VirtualFile notebookFile) {
        super(module, notebookFile);
    }

    protected JComponent buildView(){
        this.model = new ContainerModelImpl(module, notebookFile);

        ContainerModelReader reader = new ContainerModelReader(notebookFile.getParent());
        try {
            reader.read(model, new ModuleComponentSource(module));
        }catch (Exception ex){
            ex.printStackTrace();
        }

        this.writeHandler = new ModelWriteHandler(module.getProject(), notebookFile.getParent(), model);

        ContainerNodeView containerNodeView = new ContainerNodeView(model, new ModuleComponentSource(module));
        containerNodeView.setRunRequestHandler(this);
        return containerNodeView;
    }

    @Override
    public void runConnection(Connection connection) {
        if (model.getDataSources().getConnections().contains(connection)){
            runDataSource(connection);
        }
    }

    private void runDataSource(Connection connection) {
        if (model.getDataSources().getConnections().contains(connection)){
            runDataSource(connection);
        }
    }

    private static class ModelWriteHandler implements ContainerModelListener, ConnectionListListener, ConnectionListener, KeyListener {

        private final ContainerModelWriter writer;
        private final VirtualFile nodeFolder;

        public ModelWriteHandler(Project project, VirtualFile nodeFolder, ContainerModelImpl model){
            this.nodeFolder = nodeFolder;
            writer = new ContainerModelWriter(project, model);
            model.addContainerModelListener(this);
            model.getDataSources().addConnectionListListener(this);
            for (Connection c:model.getDataSources().getConnections()){
                c.addConnectionListener(this);
                for (Key key:c.getKeys()){
                    key.addKeyListener(this);
                }
            }
            model.getDataSinks().addConnectionListListener(this);
            for (Connection c:model.getDataSinks().getConnections()){
                c.addConnectionListener(this);
                for (Key key:c.getKeys()){
                    key.addKeyListener(this);
                }
            }
        }

        private void save(){
            try {
                writer.write(nodeFolder);
            }catch (Exception ex){
                ex.printStackTrace();
            }
        }

        @Override
        public void connectionAdded(ConnectionListEvent event) {
            event.getConnection().addConnectionListener(this);
            save();
        }

        @Override
        public void connectionRemoved(ConnectionListEvent event) {
            event.getConnection().removeConnectionListener(this);
            save();
        }

        @Override
        public void nameChanged(ConnectionEvent event) {
            save();
        }

        @Override
        public void testAdded(ConnectionEvent event) {
            save();
        }

        @Override
        public void testChanged(ConnectionEvent event) {
            save();
        }

        @Override
        public void testRemoved(ConnectionEvent event) {
            save();
        }

        @Override
        public void variableAdded(ConnectionEvent event) {
            save();
        }

        @Override
        public void variableRemoved(ConnectionEvent event) {
            save();
        }

        @Override
        public void keyAdded(ConnectionEvent event) {
            event.getKey().addKeyListener(this);
            save();
        }

        @Override
        public void keyRemoved(ConnectionEvent event) {
            event.getKey().removeKeyListener(this);
            save();
        }

        @Override
        public void variableAdded(KeyEvent event) {
            save();
        }

        @Override
        public void variableRemoved(KeyEvent event) {
            save();
        }

        @Override
        public void keyChanged(KeyEvent event) {
            save();
        }

        @Override
        public void inputFilesAdded(ContainerModelEvent event) {
            save();
        }

        @Override
        public void inputFilesRemoved(ContainerModelEvent event) {
            save();
        }

        @Override
        public void variableAssignmentAdded(ContainerModelEvent event) {
            save();
        }

        @Override
        public void variableAssignmentRemoved(ContainerModelEvent event) {
            save();
        }

        @Override
        public void nodePropertyChanged(ContainerModelEvent event) {
            save();
        }

        @Override
        public void inputFileRenamed(ContainerModelEvent containerModelEvent) {
            save();
        }
    }

    @Override
    public @Nls(capitalization = Nls.Capitalization.Title) @NotNull String getName() {
        return "Container Node "+notebookFile.getParent().getName();
    }


}
