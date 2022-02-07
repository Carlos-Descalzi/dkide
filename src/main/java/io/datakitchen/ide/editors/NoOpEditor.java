package io.datakitchen.ide.editors;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;

public class NoOpEditor extends AbstractNodeEditor {


    public NoOpEditor(Project project, VirtualFile file) {
        super(project, file);
    }

    @Override
    protected void disableEvents() {

    }

    @Override
    protected void enableEvents() {

    }

    @Override
    protected Map<String, JComponent> getTabs() {
        return new HashMap<>();
    }

    protected void doLoadDocument(Map<String,Object> document) {
    }

    protected void doSaveDocument(Map<String,Object> document) {
    }

    @Override
    public @Nls(capitalization = Nls.Capitalization.Title) @NotNull String getName() {
        return "NoOp Node Editor";
    }

}
