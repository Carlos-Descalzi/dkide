package io.datakitchen.ide.editors.neweditors.noop;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.vfs.VirtualFile;
import io.datakitchen.ide.editors.neweditors.AbstractNodeModel;
import io.datakitchen.ide.model.*;

public class NoOpNodeModelImpl extends AbstractNodeModel implements NoOpNodeModel {


    public NoOpNodeModelImpl(Module module, VirtualFile nodeFolder) {
        super(module,nodeFolder);
    }

}
