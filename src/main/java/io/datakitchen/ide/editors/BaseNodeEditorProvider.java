package io.datakitchen.ide.editors;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorPolicy;
import com.intellij.openapi.fileEditor.FileEditorProvider;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import io.datakitchen.ide.Constants;
import io.datakitchen.ide.config.ConfigurationService;
import io.datakitchen.ide.config.MiscOptions;
import io.datakitchen.ide.util.JsonUtil;
import io.datakitchen.ide.util.ObjectUtil;
import io.datakitchen.ide.util.RecipeUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public abstract class BaseNodeEditorProvider implements FileEditorProvider, DumbAware {

    private static final Logger LOGGER = Logger.getInstance(BaseNodeEditorProvider.class);

    @Override
    public boolean accept(@NotNull Project project, @NotNull VirtualFile file) {
        if (!RecipeUtil.isCustomEditorEnabled()){
            return false;
        }
        ConfigurationService service = ConfigurationService.getInstance(project);
        MiscOptions options = service.getGlobalConfiguration().getMiscOptions();
        if (options.isUseCustomForms() && options.isCustomNodeFormsEnabled()) {
            if (!file.getName().equals(Constants.FILE_NOTEBOOK_JSON)) {
                return false;
            }
            try {
                Map<String, Object> obj = JsonUtil.read(file.getParent().findChild(Constants.FILE_DESCRIPTION_JSON));

                return acceptNode(file, obj);

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return false;
    }

    protected boolean acceptNode(VirtualFile notebookFile, Map<String, Object> description){
        return getNodeTypeName().equals(description.get("type"));
    }

    protected boolean useSimplifiedEditor(Project project, Map<String, Object> descriptionJson){
        Map<String, Object> nodeOptions = ObjectUtil.cast(descriptionJson.get("options"));
        if (nodeOptions == null || !(Boolean)nodeOptions.getOrDefault("simplified-view",false)){
            return false;
        }
        ConfigurationService service = ConfigurationService.getInstance(project);

        return service.getGlobalConfiguration().getMiscOptions().isSimplifiedView();
    }

    protected boolean useCustomForm(Project project, VirtualFile file, Map<String, Object> descriptionJson){
        Map<String, Object> nodeOptions = ObjectUtil.cast(descriptionJson.get("options"));
        if (nodeOptions == null){
            return false;
        }
        if (RecipeUtil.isLibrary(RecipeUtil.recipeFolder(project,file))){
            return false;
        }
        if (!(Boolean)nodeOptions.getOrDefault("library-asset", true)){
            return false;
        }
        return nodeOptions.get("custom-form") != null;
    }

    @Override
    public @NotNull FileEditor createEditor(@NotNull Project project, @NotNull VirtualFile file) {
        try {
            Map<String, Object> obj = JsonUtil.read(file.getParent().findChild(Constants.FILE_DESCRIPTION_JSON));
            if (useCustomForm(project, file, obj)){
                return createCustomFormEditor(project, file);
            }
            if (useSimplifiedEditor(project, obj)){
                return createSimplifiedEditor(project, file);
            }
        }catch (Exception ex){
            LOGGER.error(ex);
        }

        return createRegularEditor(project, file);
    }

    private FileEditor createCustomFormEditor(Project project, VirtualFile file) {
        Module module = ModuleUtil.findModuleForFile(file, project);
        return new CustomNodeEditor(module, file);
    }

    @Override
    public @NotNull @NonNls String getEditorTypeId() {
        return getNodeTypeName().toLowerCase().replace("_","-")+"-editor";
    }

    @Override
    public final @NotNull FileEditorPolicy getPolicy() {
        return FileEditorPolicy.HIDE_DEFAULT_EDITOR;
    }

    protected abstract String getNodeTypeName();

    protected abstract FileEditor createRegularEditor(Project project, VirtualFile file);

    protected abstract FileEditor createSimplifiedEditor(Project project, VirtualFile file);

}
