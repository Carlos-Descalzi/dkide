package io.datakitchen.ide.editors.neweditors;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.vfs.VirtualFile;
import io.datakitchen.ide.editors.neweditors.action.ActionNodeModelImpl;
import io.datakitchen.ide.editors.neweditors.action.ActionNodeModelReader;
import io.datakitchen.ide.editors.neweditors.action.ActionNodeModelWriter;
import io.datakitchen.ide.editors.neweditors.action.ActionNodeView;
import io.datakitchen.ide.editors.neweditors.palette.ModuleComponentSource;
import io.datakitchen.ide.model.*;
import io.datakitchen.ide.run.DataSourceRunner;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class ActionEditor
    extends BaseSimpleEditor
    implements
        RunRequestHandler, NodeModelListener {

    private static final Logger LOGGER = Logger.getInstance(ActionEditor.class);

    private ActionNodeModelWriter writer;
    public ActionEditor(Module module, VirtualFile notebookFile) {
        super(module, notebookFile);
    }

    @Override
    protected JComponent buildView() {
        ActionNodeModelImpl model = new ActionNodeModelImpl(module,notebookFile.getParent());
        ActionNodeModelReader reader = new ActionNodeModelReader(
                model,
                notebookFile.getParent(),
                new ModuleComponentSource(module)
        );
        try {
            reader.read();
        }catch(Exception ex){
            LOGGER.error(ex);
        }
        writer = new ActionNodeModelWriter(model, notebookFile.getParent());
        model.addNodeModelListener(this);
        new ModelWriteHandler(this::save).setup(model.getConnectionList());
        ActionNodeView view = new ActionNodeView(model, new ModuleComponentSource(module));
        view.setRunRequestHandler(this);
        return view;
    }

    protected void save() {
        ApplicationManager.getApplication().runWriteAction(()->{
            try {
                writer.write();
            }catch(Exception ex){
                LOGGER.error(ex);
            }
        });
    }

    @Override
    public void testAdded(NodeModelEvent actionNodeModelEvent) {
        save();
    }

    @Override
    public void testRemoved(NodeModelEvent actionNodeModelEvent) {
        save();
    }

    @Override
    public void nodePropertyChanged(NodeModelEvent description) {
        save();
    }

    @Override
    public @Nls(capitalization = Nls.Capitalization.Title) @NotNull String getName() {
        return "Action Node "+notebookFile.getParent().getName();
    }

    @Override
    public void runConnection(Connection connection) {
        VirtualFile actionsFolder = notebookFile.getParent().findChild("actions");
        if (actionsFolder != null) {
            VirtualFile dsFile = actionsFolder.findChild(connection.getName() + ".json");
            if (dsFile != null) {
                new DataSourceRunner(module, dsFile).run();
            }
        }
    }
}
