package io.datakitchen.ide.editors.neweditors;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.vfs.VirtualFile;
import io.datakitchen.ide.editors.neweditors.ingredient.IngredientNodeModelImpl;
import io.datakitchen.ide.editors.neweditors.ingredient.IngredientNodeModelReader;
import io.datakitchen.ide.editors.neweditors.ingredient.IngredientNodeModelWriter;
import io.datakitchen.ide.editors.neweditors.ingredient.IngredientNodeView;
import io.datakitchen.ide.model.NodeModelEvent;
import io.datakitchen.ide.model.NodeModelListener;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class IngredientEditor
    extends BaseSimpleEditor
    implements NodeModelListener {

    private IngredientNodeModelWriter writer;

    public IngredientEditor(Module module, VirtualFile notebookFile) {
        super(module, notebookFile);
    }

    @Override
    protected JComponent buildView() {
        IngredientNodeModelImpl model = new IngredientNodeModelImpl(module, notebookFile.getParent());
        IngredientNodeModelReader reader = new IngredientNodeModelReader(model, notebookFile.getParent());
        try {
            reader.read();
        }catch(Exception ex){
            ex.printStackTrace();
        }
        IngredientNodeView view = new IngredientNodeView(model);
        model.addNodeModelListener(this);
        writer = new IngredientNodeModelWriter(model, notebookFile.getParent());
        return view;
    }


    private void save(){
        ApplicationManager.getApplication().runWriteAction(()->{
            try {
                writer.write();
            }catch( Exception ex){
                ex.printStackTrace();
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
        return "Ingredient Node "+notebookFile.getParent().getName();
    }
}
