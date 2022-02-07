package io.datakitchen.ide.editors;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;

public class ActionEditor extends AbstractNodeEditor {


    public ActionEditor(Project project, VirtualFile file) {
        super(project, file);
    }

    @Override
    protected Map<String, JComponent> getTabs() {
        return new HashMap<>();
    }

    @Override
    protected void doLoadDocument(Map<String,Object> document) {

    }

    @Override
    protected void doSaveDocument(Map<String,Object> document) {

    }

    @Override
    public @Nls(capitalization = Nls.Capitalization.Title) @NotNull String getName() {
        return "Action Node Editor";
    }

    @Override
    protected void disableEvents() {

    }

    @Override
    protected void enableEvents() {

    }
}
