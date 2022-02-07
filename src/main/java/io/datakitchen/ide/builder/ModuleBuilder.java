package io.datakitchen.ide.builder;

import com.intellij.facet.FacetManager;
import com.intellij.facet.FacetType;
import com.intellij.facet.FacetTypeId;
import com.intellij.facet.FacetTypeRegistry;
import com.intellij.ide.projectView.ProjectView;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.libraries.LibraryTablesRegistrar;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import io.datakitchen.ide.module.RecipeModuleType;
import io.datakitchen.ide.service.RecipeModuleSettingsService;
import io.datakitchen.ide.util.RecipeUtil;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ModuleBuilder {

    private final Project project;
    private Path recipePath;
    private final Map<String, String> moduleProperties = new LinkedHashMap<>();

    public ModuleBuilder(Project project){
        this.project = project;
    }

    public ModuleBuilder setProperty(String key, String value){
        moduleProperties.put(key, value);
        return this;
    }

    public ModuleBuilder setRecipePath(Path recipePath) {
        this.recipePath = recipePath;
        return this;
    }

    public void build(){
        Module module = createRecipeModule(project, recipePath);

        RecipeModuleSettingsService settingsService = RecipeModuleSettingsService.getInstance(module);

        for (Map.Entry<String, String> entry: moduleProperties.entrySet()){
            settingsService.setProperty(entry.getKey(), entry.getValue());
        }
    }

    private Module createRecipeModule(Project project, Path folderPath) {
        Path modulePath = Path.of(folderPath.toString(),folderPath.getFileName().toString());
        Module module = ModuleManager.getInstance(project).newModule(modulePath, RecipeModuleType.MODULE_TYPE_ID);

        VirtualFile moduleFolder = VirtualFileManager.getInstance().refreshAndFindFileByNioPath(folderPath);
        ModifiableRootModel model = ModuleRootManager.getInstance(module).getModifiableModel();
        configureSdkAndFramework(module, model);
        model.addContentEntry(moduleFolder);

        model.commit();

        List<String> variations = RecipeUtil.getVariations(module);

        if (!variations.isEmpty()){
            RecipeUtil.setActiveVariation(module, variations.get(0));
        }

        ProjectView.getInstance(project).refresh();

        return module;
    }

    @SuppressWarnings({"unchecked","rawtypes"})
    private void configureSdkAndFramework(Module module, ModifiableRootModel model) {

        for (Library library: LibraryTablesRegistrar.getInstance().getLibraryTable().getLibraries()){
            String name = String.valueOf(library.getName());
            if (name.contains("Python") && name.contains("interpreter library")){
                model.addLibraryEntry(library);
            }
        }

        FacetTypeRegistry registry = FacetTypeRegistry.getInstance();

        Arrays.stream(registry.getFacetTypeIds())
                .filter((FacetTypeId id)->id.toString().equals("python"))
                .findFirst()
                .ifPresent((FacetTypeId id)->{
                    FacetType facetType = registry.findFacetType(id);
                    FacetManager.getInstance(module).addFacet(facetType,"Python",null);
                });
    }

}
