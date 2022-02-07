package io.datakitchen.ide.editors.neweditors;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import io.datakitchen.ide.editors.neweditors.mapper.DataMapperModelImpl;
import io.datakitchen.ide.editors.neweditors.mapper.DataMapperModelReader;
import io.datakitchen.ide.editors.neweditors.mapper.DataMapperModelWriter;
import io.datakitchen.ide.editors.neweditors.mapper.DataMapperNodeView;
import io.datakitchen.ide.editors.neweditors.palette.ModuleComponentSource;
import io.datakitchen.ide.model.*;
import io.datakitchen.ide.run.DataSourceRunner;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class DataMapperEditor extends BaseSimpleEditor implements RunRequestHandler {

    private DataMapperModelImpl model;
    private ModelWriteHandler writeHandler;
    
    public DataMapperEditor(Module module, VirtualFile notebookFile) {
        super(module, notebookFile);
    }

    protected JComponent buildView(){
        this.model = new DataMapperModelImpl(module);

        DataMapperModelReader reader = new DataMapperModelReader(module.getProject(), model);
        try {
            reader.read(notebookFile.getParent(), new ModuleComponentSource(module));
        }catch (Exception ex){
            ex.printStackTrace();
        }

        this.writeHandler = new ModelWriteHandler(module.getProject(), notebookFile.getParent(), this.model);

        DataMapperNodeView view = new DataMapperNodeView(this.model, new ModuleComponentSource(module));
        view.setRunRequestHandler(this);
        return view;
    }

    @Override
    public @Nls(capitalization = Nls.Capitalization.Title) @NotNull String getName() {
        return "Data Mapper Node "+ notebookFile.getParent().getName();
    }

    @Override
    public void runConnection(Connection connection) {
        if (model.getDataSources().getConnections().contains(connection)){
            runDataSource(connection);
        }
    }

    private void runDataSource(Connection connection) {
        VirtualFile dataSourcesFolder = notebookFile.getParent().findChild("data_sources");
        VirtualFile dsFile = dataSourcesFolder.findChild(connection.getName()+".json");
        if(dsFile != null){
            new DataSourceRunner(module, dsFile)
                    .run();
        }
    }

    private void runDataSink(Connection connection) {
    }

    private static class ModelWriteHandler
            implements DataMapperModelListener,
                ConnectionListListener,
                ConnectionListener,
                KeyListener{

        private final DataMapperModelWriter writer;
        private final VirtualFile nodeFolder;

        public ModelWriteHandler(Project project, VirtualFile nodeFolder, DataMapperModelImpl model){
            this.nodeFolder = nodeFolder;
            writer = new DataMapperModelWriter(project, model);
            model.addDataMapperModelListener(this);
            model.getDataSources().addConnectionListListener(this);
            for (Connection c:model.getDataSources().getConnections()){
                c.addConnectionListener(this);
                for (Key k: c.getKeys()){
                    k.addKeyListener(this);
                }
            }
            model.getDataSinks().addConnectionListListener(this);
            for (Connection c: model.getDataSinks().getConnections()){
                c.addConnectionListener(this);
                for (Key k: c.getKeys()){
                    k.addKeyListener(this);
                }
            }
        }

        @Override
        public void mappingsAdded(DataMapperModelEvent event) {
            save();
        }

        @Override
        public void mappingsRemoved(DataMapperModelEvent event) {
            save();
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
            save();
            event.getKey().addKeyListener(this);
        }

        @Override
        public void keyRemoved(ConnectionEvent event) {
            save();
            event.getKey().removeKeyListener(this);
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
        public void nodePropertyChanged(DataMapperModelEvent event) {
            save();
        }

        private void save(){
            try {
                writer.write(nodeFolder);
            }catch (Exception ex){
                ex.printStackTrace();
            }
        }

    }

}
