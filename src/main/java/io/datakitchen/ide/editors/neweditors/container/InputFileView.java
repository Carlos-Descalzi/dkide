package io.datakitchen.ide.editors.neweditors.container;

import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.vfs.VirtualFile;
import io.datakitchen.ide.editors.neweditors.EditorIcons;
import io.datakitchen.ide.ui.LabelWithActions;
import io.datakitchen.ide.ui.SimpleAction;

public class InputFileView extends LabelWithActions {

    private final Module module;
    private final VirtualFile file;

    public InputFileView(Module module, VirtualFile file, ActionSupplier actions){
        super("",null,actions);
        this.module = module;
        this.file = file;

        if (file.isDirectory()){
            label.setText("docker-share/"+file.getName()+"/");
        } else {
            label.setText("docker-share/"+file.getName());
            label.setIcon(EditorIcons.FILE);
        }
        setDoubleClickAction(new SimpleAction("", e -> openFile()));
    }

    private void openFile() {
        FileEditorManager.getInstance(module.getProject()).openFile(file, true);
    }

    public VirtualFile getFile() {
        return file;
    }
}
