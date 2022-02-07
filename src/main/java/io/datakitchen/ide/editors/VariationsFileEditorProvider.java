package io.datakitchen.ide.editors;

import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorPolicy;
import com.intellij.openapi.fileEditor.FileEditorProvider;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import io.datakitchen.ide.Constants;
import io.datakitchen.ide.config.ConfigurationService;
import io.datakitchen.ide.editors.neweditors.variation.VariationsEditor;
import io.datakitchen.ide.util.JsonUtil;
import io.datakitchen.ide.util.ObjectUtil;
import io.datakitchen.ide.util.RecipeUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;

public class VariationsFileEditorProvider implements FileEditorProvider, DumbAware {
    @Override
    public boolean accept(@NotNull Project project, @NotNull VirtualFile file) {
        if (!RecipeUtil.isCustomEditorEnabled()){
            return false;
        }
        if (ConfigurationService.getInstance(project).getGlobalConfiguration().getMiscOptions().isUseCustomForms()) {
            return file.getName().equals(Constants.FILE_VARIATIONS_JSON);
        }
        return false;
    }

    private boolean useSimplifiedForm(Project project, VirtualFile file){
        try {
            Map<String, Object> obj = JsonUtil.read(Objects.requireNonNull(
                    file.getParent().findChild(Constants.FILE_DESCRIPTION_JSON)));
            Map<String, Object> nodeOptions = ObjectUtil.cast(obj.get("options"));
            if (nodeOptions == null || !(Boolean) nodeOptions.getOrDefault("simplified-view", false)) {
                return false;
            }
            ConfigurationService service = ConfigurationService.getInstance(project);

            return service.getGlobalConfiguration().getMiscOptions().isSimplifiedView();
        }catch(Exception ex){
            return false;
        }
    }

    @Override
    public @NotNull FileEditor createEditor(@NotNull Project project, @NotNull VirtualFile file) {
        if (useSimplifiedForm(project, file)){
            return new VariationsEditor(project, file);
        }
        return new VariationsFileEditor(project, file);
    }

    @Override
    public @NotNull @NonNls String getEditorTypeId() {
        return "recipe-files-variation";
    }

    @Override
    public @NotNull FileEditorPolicy getPolicy() {
        return FileEditorPolicy.HIDE_DEFAULT_EDITOR;
    }
}
