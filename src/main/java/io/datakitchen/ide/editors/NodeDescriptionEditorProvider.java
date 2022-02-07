package io.datakitchen.ide.editors;

import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorPolicy;
import com.intellij.openapi.fileEditor.FileEditorProvider;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import io.datakitchen.ide.Constants;
import io.datakitchen.ide.config.ConfigurationService;
import io.datakitchen.ide.util.RecipeUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class NodeDescriptionEditorProvider implements FileEditorProvider, DumbAware {
    @Override
    public boolean accept(@NotNull Project project, @NotNull VirtualFile file) {
        if (!RecipeUtil.isCustomEditorEnabled()){
            return false;
        }
        if (ConfigurationService.getInstance(project).getGlobalConfiguration().getMiscOptions().isUseCustomForms()) {
            return file.getName().equals(Constants.FILE_DESCRIPTION_JSON)
                    && file.getParent().findChild(Constants.FILE_VARIATIONS_JSON) == null;
        }
        return false;
    }

    @Override
    public @NotNull FileEditor createEditor(@NotNull Project project, @NotNull VirtualFile file) {
        return new NodeDescriptionEditor(project, file);
    }

    @Override
    public @NotNull @NonNls String getEditorTypeId() {
        return "node-description";
    }

    @Override
    public @NotNull FileEditorPolicy getPolicy() {
        return FileEditorPolicy.HIDE_DEFAULT_EDITOR;
    }
}
