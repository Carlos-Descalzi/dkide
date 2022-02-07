package io.datakitchen.ide.editors;


import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorPolicy;
import com.intellij.openapi.fileEditor.FileEditorProvider;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import io.datakitchen.ide.service.ContainerService;
import io.datakitchen.ide.util.RecipeUtil;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Set;

public class CompiledFileEditorProvider implements FileEditorProvider {

    private static final Set<String> EXTENSIONS = Set.of("json","xml","html","txt","sql");

    @Override
    public boolean accept(@NotNull Project project, @NotNull VirtualFile file) {

        boolean serviceAvailable = ContainerService.getInstance(
                Objects.requireNonNull(project)).isServiceAvailable();

        return serviceAvailable
                && (validExtension(file.getName())
                && ModuleUtil.findModuleForFile(file,project) != null
                && !file.getName().equals("local-overrides.json"))
                || isResourceFile(project, file);
    }

    private boolean validExtension(String fileName){
        String extension = StringUtils.substringAfterLast(fileName.toLowerCase(),".");
        return EXTENSIONS.contains(extension);
    }

    private boolean isResourceFile(Project project, VirtualFile file) {
        VirtualFile recipeFolder = RecipeUtil.recipeFolder(ModuleUtil.findModuleForFile(file, project));
        if (recipeFolder == null){
            return false;
        }
        VirtualFile resourcesFolder = recipeFolder.findChild("resources");
        if (resourcesFolder == null){
            return false;
        }
        return file.getPath().contains(resourcesFolder.getPath());
    }

    @Override
    public @NotNull FileEditor createEditor(@NotNull Project project, @NotNull VirtualFile file) {
        return new CompiledFileEditorView(project, file);
    }

    @Override
    public @NotNull @NonNls String getEditorTypeId() {
        return "compiled-file-preview";
    }

    @Override
    public @NotNull FileEditorPolicy getPolicy() {
        return FileEditorPolicy.PLACE_AFTER_DEFAULT_EDITOR;
    }
}
