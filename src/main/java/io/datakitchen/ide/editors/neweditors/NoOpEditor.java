package io.datakitchen.ide.editors.neweditors;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.vfs.VirtualFile;
import io.datakitchen.ide.editors.neweditors.noop.NoOpNodeModelImpl;
import io.datakitchen.ide.editors.neweditors.noop.NoOpNodeModelReader;
import io.datakitchen.ide.editors.neweditors.noop.NoOpNodeModelWriter;
import io.datakitchen.ide.editors.neweditors.noop.NoOpNodeView;
import io.datakitchen.ide.model.NodeModelEvent;
import io.datakitchen.ide.model.NodeModelListener;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class NoOpEditor
    extends BaseSimpleEditor
    implements NodeModelListener {

    private static final Logger LOGGER = Logger.getInstance(NoOpEditor.class);

    private NoOpNodeModelWriter writer;

    public NoOpEditor(Module module, VirtualFile notebookFile) {
        super(module, notebookFile);
    }

    @Override
    protected JComponent buildView() {
        NoOpNodeModelImpl model = new NoOpNodeModelImpl(module, notebookFile.getParent());
        NoOpNodeModelReader reader = new NoOpNodeModelReader(model, notebookFile.getParent());
        try {
            reader.read();
        }catch(Exception ex){
            LOGGER.error(ex);
        }
        NoOpNodeView view = new NoOpNodeView(model);
        model.addNodeModelListener(this);
        writer = new NoOpNodeModelWriter(model, notebookFile.getParent());
        return view;
    }


    private void save(){
        ApplicationManager.getApplication().runWriteAction(()->{
            try {
                writer.write();
            }catch( Exception ex){
                LOGGER.error(ex);
            }
        });
    }

    @Override
    public void testAdded(NodeModelEvent event) {
        save();
    }

    @Override
    public void testRemoved(NodeModelEvent event) {
        save();
    }

    @Override
    public void nodePropertyChanged(NodeModelEvent description) {
        save();
    }

    @Override
    public @Nls(capitalization = Nls.Capitalization.Title) @NotNull String getName() {
        return "NoOp Node "+notebookFile.getParent().getName();
    }
}
