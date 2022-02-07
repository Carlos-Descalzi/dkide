package io.datakitchen.ide.module;

import com.intellij.ide.util.projectWizard.ModuleBuilder;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.ProjectJdkTable;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkTypeId;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ProjectRootManager;
import io.datakitchen.ide.util.RecipeUtil;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.Arrays;

public class EmptyModuleBuilder extends ModuleBuilder {
    @Override
    public ModuleType<?> getModuleType() {
        return ModuleType.EMPTY;
    }
    public boolean canCreateModule() {
        return false;
    }
    public boolean isOpenProjectSettingsAfter() {
        return false;
    }

    public boolean isSuitableSdkType(SdkTypeId sdkType) {
        return validSdkType(sdkType);
    }

    public @Nullable Project createProject(String name, String path) {
        Project project = super.createProject(name, path);

        if (project != null) {
            Arrays.stream(ProjectJdkTable.getInstance().getAllJdks())
                .filter(this::validSdk)
                .findFirst()
                .ifPresent(sdk -> ApplicationManager.getApplication().runWriteAction(()->{
                    ProjectRootManager.getInstance(project).setProjectSdk(sdk);
                }));

            ApplicationManager.getApplication().runWriteAction(()->{
                Module module = ModuleManager.getInstance(project).newModule(
                        Path.of(project.getBasePath(),project.getName()),
                        WorkspaceSettingsModuleType.MODULE_TYPE_ID);

                ModifiableRootModel model = ModuleRootManager.getInstance(module).getModifiableModel();
                try {
                    Path modulePath = Path.of(project.getBasePath());
                    RecipeUtil.createLocalOverridesFile(modulePath.toFile().getAbsolutePath(), model);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                model.commit();
            });
        }
        return project;
    }

    private boolean validSdk(Sdk sdk){
        return validSdkType(sdk.getSdkType())
                && sdk.getHomeDirectory() != null;
    }

    private boolean validSdkType(SdkTypeId sdkType){
        return sdkType.getName().toLowerCase().contains("python");
    }
}
