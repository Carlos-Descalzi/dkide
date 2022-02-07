package io.datakitchen.ide.editors.neweditors;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import io.datakitchen.ide.editors.neweditors.container.*;
import io.datakitchen.ide.editors.neweditors.palette.ModuleComponentSource;
import io.datakitchen.ide.model.*;
import io.datakitchen.ide.run.DataSourceRunner;
import io.datakitchen.ide.run.ScriptDebugger;
import io.datakitchen.ide.run.ScriptRunner;
import io.datakitchen.ide.ui.SimpleAction;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class ScriptNodeEditor extends BaseSimpleEditor implements RunRequestHandler{

    private ScriptNodeModelImpl model;
    private ScriptNodeView containerNodeView;
    private ModelWriteHandler writeHandler;

    public ScriptNodeEditor(Module module, VirtualFile notebookFile) {
        super(module, notebookFile);
    }

    protected JComponent buildView(){
        this.model = new ScriptNodeModelImpl(module, notebookFile);

        ScriptNodeModelReader reader = new ScriptNodeModelReader(notebookFile.getParent());
        try {
            reader.read(model, new ModuleComponentSource(module));
        }catch (Exception ex){
            ex.printStackTrace();
        }

        this.writeHandler = new ModelWriteHandler(module.getProject(), notebookFile.getParent(), model);

        this.containerNodeView = new ScriptNodeView(model,new ModuleComponentSource(module));
        this.containerNodeView.setRunRequestHandler(this);
        this.containerNodeView.setFileActionSupplier(this::getFileActions);
        return this.containerNodeView;
    }

    private List<Action> getFileActions(VirtualFile virtualFile) {
        List<Action> actions = new ArrayList<>();
        if (virtualFile.getName().endsWith(".py")){
            actions.add(new SimpleAction(AllIcons.Actions.Run_anything, "Run", "Run Script", e -> runScript(virtualFile)));

            if (ScriptDebugger.isDebugEnabled()){
                actions.add(new SimpleAction(AllIcons.Actions.StartDebugger,"Debug", "Debug Script", e ->debugScript(virtualFile)));
            }
        }
        return actions;
    }

    private void debugScript(VirtualFile virtualFile) {
        new ScriptDebugger(model.getModule(), virtualFile)
                .run();
    }

    private void runScript(VirtualFile virtualFile) {
        new ScriptRunner(model.getModule(), virtualFile)
                .run();
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


    private class ModelWriteHandler implements ContainerModelListener, ScriptNodeModelListener, ConnectionListListener, ConnectionListener, KeyListener {

        private final ScriptModelWriter writer;
        private final VirtualFile nodeFolder;

        public ModelWriteHandler(Project project, VirtualFile nodeFolder, ContainerModelImpl model){
            this.nodeFolder = nodeFolder;
            writer = new ScriptModelWriter(project, model);
            model.addContainerModelListener(this);
            ((ScriptNodeModel)model).addScriptNodeModelListener(this);
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

        @Override
        public void keyAdded(ScriptNodeModelEvent event) {
            save();
        }

        @Override
        public void keyRemoved(ScriptNodeModelEvent event) {
            save();
        }

        @Override
        public void keyChanged(ScriptNodeModelEvent scriptNodeModelEvent) {
            save();
        }

        @Override
        public void propertyChanged(ScriptNodeModelEvent scriptNodeModelEvent) {
            save();
        }
    }

    @Override
    public @Nls(capitalization = Nls.Capitalization.Title) @NotNull String getName() {
        return "Script Node "+notebookFile.getParent().getName();
    }


}
