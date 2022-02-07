package io.datakitchen.ide.editors.neweditors.variation;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.newvfs.BulkFileListener;
import com.intellij.openapi.vfs.newvfs.events.VFileDeleteEvent;
import com.intellij.openapi.vfs.newvfs.events.VFileEvent;
import com.intellij.openapi.vfs.newvfs.events.VFilePropertyChangeEvent;
import com.intellij.util.messages.MessageBusConnection;
import io.datakitchen.ide.editors.AbstractFileEditor;
import io.datakitchen.ide.util.RecipeUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.List;
import java.util.Objects;

public class VariationsEditor extends AbstractFileEditor {

    private final Project project;
    private final VirtualFile file;
    private final RecipeVariationsEditor editor;

    private final MessageBusConnection connection;

    public VariationsEditor(Project project, VirtualFile file) {
        super();
        this.project = project;
        this.file = file;
        this.editor = new RecipeVariationsEditor(project, file);
        Disposer.register(this, this.editor);
        connection = project.getMessageBus().connect();
        connection.subscribe(VirtualFileManager.VFS_CHANGES, new BulkFileListener() {
            @Override
            public void after(@NotNull List<? extends @NotNull VFileEvent> events) {
                filesUpdated(events);
            }

            @Override
            public void before(@NotNull List<? extends @NotNull VFileEvent> events) {
                checkRemoved(events);
            }
        });
    }

    public void dispose(){
        super.dispose();
        connection.disconnect();
    }

    private void checkRemoved(List<? extends VFileEvent> events) {
        for (VFileEvent event:events){
            if (event instanceof VFileDeleteEvent){
                Module module = ModuleUtil.findModuleForFile(Objects.requireNonNull(event.getFile()),project);
                if (module != null && RecipeUtil.isNodeFolder(module,event.getFile())) {
                    editor.notifyNodeRemoved(event.getFile().getName());
                }
            }
        }
    }
    private void filesUpdated(List<? extends VFileEvent> events) {
        for (VFileEvent event:events){
            if (event instanceof VFilePropertyChangeEvent){
                Module module = ModuleUtil.findModuleForFile(Objects.requireNonNull(event.getFile()),project);
                if (module != null && RecipeUtil.isNodeFolder(module,event.getFile())){
                    editor.notifyNodeRenamed(
                            (String)((VFilePropertyChangeEvent)event).getOldValue(),
                            (String)((VFilePropertyChangeEvent)event).getNewValue());
                }
            }
        }

    }
    public @NotNull VirtualFile getFile(){
        return file;
    }

    @Override
    public @NotNull JComponent getComponent() {
        return editor;
    }

    @Override
    public @Nullable JComponent getPreferredFocusedComponent() {
        return null;
    }

    @Override
    public @Nls(capitalization = Nls.Capitalization.Title) @NotNull String getName() {
        return "Recipe Variations";
    }
}
