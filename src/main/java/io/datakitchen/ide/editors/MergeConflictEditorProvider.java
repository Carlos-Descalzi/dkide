package io.datakitchen.ide.editors;

import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorPolicy;
import com.intellij.openapi.fileEditor.FileEditorProvider;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import io.datakitchen.ide.editors.diff.PlainDiffEditor;
import io.datakitchen.ide.editors.diff.DiffFileLoader;
import io.datakitchen.ide.util.RecipeUtil;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

public class MergeConflictEditorProvider implements FileEditorProvider, DumbAware {

    /**
     * TODO make it more efficient, check if file read can be avoided.
     * @param project
     * @param file
     * @return
     */
    @Override
    public boolean accept(@NotNull Project project, @NotNull VirtualFile file) {
        if (!RecipeUtil.isCustomEditorEnabled()){
            return false;
        }
        try (InputStream input = file.getInputStream()){
            String content = IOUtils.toString(input, Charset.defaultCharset());
            return content.contains(DiffFileLoader.DIFF_LOCAL)
                    || content.contains(DiffFileLoader.DIFF_REMOTE);

        }catch (IOException ex){
            return false;
        }
    }

    @Override
    public @NotNull FileEditor createEditor(@NotNull Project project, @NotNull VirtualFile file) {
        if (file.getName().endsWith(".json")) {
            return new MergeConflictEditor(project, file);
        }

        try {
            return new PlainDiffEditor(project, file);
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    @Override
    public @NotNull @NonNls String getEditorTypeId() {
        return "merge-conflict-editor";
    }

    @Override
    public @NotNull FileEditorPolicy getPolicy() {
        return FileEditorPolicy.PLACE_BEFORE_DEFAULT_EDITOR;
    }
}
