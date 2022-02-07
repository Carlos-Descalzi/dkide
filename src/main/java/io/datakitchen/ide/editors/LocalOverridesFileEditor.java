package io.datakitchen.ide.editors;

import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.VirtualFile;
import io.datakitchen.ide.editors.overrides.LocalOverridesEditor;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class LocalOverridesFileEditor extends AbstractFileEditor implements DumbAware {

    protected final Project project;
    protected final VirtualFile file;
    private final LocalOverridesEditor editor;

    public LocalOverridesFileEditor(Project project, VirtualFile file) {
        this.project = project;
        this.file = file;
        this.editor = new LocalOverridesEditor(ModuleUtil.findModuleForFile(file, project), file);
        Disposer.register(this, editor);
    }

    @Override
    public void deselectNotify() {
        this.editor.save();
    }

    @Override
    public void selectNotify() {
        this.editor.load();
    }

    @Override
    public @NotNull JComponent getComponent() {
        return editor;
    }

    @Override
    public @Nullable VirtualFile getFile() {
        return file;
    }

    @Override
    public @Nullable JComponent getPreferredFocusedComponent() {
        return null;
    }

    @Override
    public @Nls(capitalization = Nls.Capitalization.Title) @NotNull String getName() {
        return "Local Overrides";
    }

}
