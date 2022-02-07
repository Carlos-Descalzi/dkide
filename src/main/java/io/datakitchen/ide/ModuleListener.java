package io.datakitchen.ide;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.project.Project;
import io.datakitchen.ide.module.RecipeModuleType;
import io.datakitchen.ide.module.WorkspaceSettingsModuleType;
import io.datakitchen.ide.service.CompilerService;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class ModuleListener implements com.intellij.openapi.project.ModuleListener {

    private static final Logger LOGGER = Logger.getInstance(ModuleListener.class);

    private static final Set<String> VALID_MODULE_IDS = Set.of(RecipeModuleType.MODULE_TYPE_ID, WorkspaceSettingsModuleType.MODULE_TYPE_ID);

    public void moduleAdded(@NotNull Project project, @NotNull Module module) {

        ModuleType<?> moduleType = ModuleType.get(module);

        if (VALID_MODULE_IDS.contains(moduleType.getId())) {
            LOGGER.info("Starting compiler service for module "+module.getName());
            new Thread(() -> CompilerService.getInstance(module).checkInit()).start();
        }
    }

}
