package io.datakitchen.ide.editors;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileEditorLocation;
import com.intellij.openapi.fileEditor.FileEditorState;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import io.datakitchen.ide.editors.diff.DiffFileLoader;
import io.datakitchen.ide.editors.diff.json.JsonDiffEditor;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

public class MergeConflictEditor extends AbstractFileEditor{

    private final Project project;
    private final VirtualFile file;
    private final JsonDiffEditor editor = new JsonDiffEditor();

    public MergeConflictEditor(Project project, VirtualFile file){
        this.project = project;
        this.file = file;

        try {
            DiffFileLoader loader = new DiffFileLoader(file);
            loader.load();
            editor.setFiles(loader.getLeft(), loader.getRight());
            editor.onFinish(this::documentChanged);
        }catch (Exception ex){
            ex.printStackTrace();
        }

    }

    private void documentChanged(String newContent){
        ApplicationManager.getApplication().runWriteAction(()->{
           try (OutputStream out = file.getOutputStream(this)){
               OutputStreamWriter writer = new OutputStreamWriter(out);
               writer.write(newContent);
               writer.flush();
           } catch (Exception ex){
               ex.printStackTrace();
           }
        });
    }

    @Override
    public @NotNull VirtualFile getFile() {
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
        return "Merge Conflict Editor";
    }

    @Override
    public void setState(@NotNull FileEditorState state) {

    }

    @Override
    public @Nullable FileEditorLocation getCurrentLocation() {
        return null;
    }

    @Override
    public void selectNotify() {

    }

}
