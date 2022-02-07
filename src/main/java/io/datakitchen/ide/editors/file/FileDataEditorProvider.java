package io.datakitchen.ide.editors.file;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorPolicy;
import com.intellij.openapi.fileEditor.FileEditorProvider;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import io.datakitchen.ide.config.ConfigurationService;
import io.datakitchen.ide.config.MiscOptions;
import io.datakitchen.ide.util.JsonUtil;
import io.datakitchen.ide.util.RecipeUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public abstract class FileDataEditorProvider implements FileEditorProvider, DumbAware {

    private static final Logger LOGGER = Logger.getInstance(FileDataEditorProvider.class);

    protected abstract String getTypeName();
    protected abstract Class<? extends FileDataEditor> getEditorClass();

    @Override
    public boolean accept(@NotNull Project project, @NotNull VirtualFile file) {
        if (!RecipeUtil.isCustomEditorEnabled()){
            return false;
        }
        ConfigurationService service = ConfigurationService.getInstance(project);
        MiscOptions options = service.getGlobalConfiguration().getMiscOptions();
        if (options.isUseCustomForms() && options.isCustomDsFormsEnabled()) {
            try {
                Map<String, Object> obj = JsonUtil.read(file);
                return getTypeName().equals(obj.get("type"));
            } catch (Exception ignored) {
            }
        }
        return false;
    }

    @Override
    public @NotNull FileEditor createEditor(@NotNull Project project, @NotNull VirtualFile file) {
        try {
            return getEditorClass().getDeclaredConstructor(Project.class, VirtualFile.class).newInstance(project, file);
        }catch (Exception ex){
            LOGGER.error(ex);
            throw new RuntimeException(ex);
        }
    }

    @Override
    public @NotNull FileEditorPolicy getPolicy() {
        return FileEditorPolicy.HIDE_DEFAULT_EDITOR;//PLACE_BEFORE_DEFAULT_EDITOR;
    }

    @Override
    public @NotNull @NonNls String getEditorTypeId() {
        return getTypeName()+"_editor";
    }

}
