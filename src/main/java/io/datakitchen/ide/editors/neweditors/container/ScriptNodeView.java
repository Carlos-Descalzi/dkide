package io.datakitchen.ide.editors.neweditors.container;

import com.intellij.openapi.vfs.VirtualFile;
import io.datakitchen.ide.editors.neweditors.palette.ComponentSource;
import io.datakitchen.ide.model.ContainerModel;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.List;
import java.util.function.Function;

public class ScriptNodeView extends ContainerNodeView{

    public ScriptNodeView(ContainerModel model, ComponentSource componentSource) {
        super(model, componentSource);
        inputFiles.setFileFilter(f -> !f.getName().equals("config.json"));
    }

    public void setFileActionSupplier(Function<VirtualFile, List<Action>> actionSupplier){
        inputFiles.setActionSupplier(actionSupplier);
    }

    @Override
    protected @NotNull ContainerView createContainerView() {
        return new GPCView(this);
    }
}
