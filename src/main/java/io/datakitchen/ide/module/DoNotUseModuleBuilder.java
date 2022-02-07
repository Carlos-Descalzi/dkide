package io.datakitchen.ide.module;

import com.intellij.ide.util.projectWizard.ModuleBuilder;
import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.roots.ui.configuration.ModulesProvider;
import org.jetbrains.annotations.NotNull;

/**
 * This builder is not really used, it is required by the module type.
 * As a side effect a module option will appear on wizard, showing a message
 * about not using it.
 */
public class DoNotUseModuleBuilder extends ModuleBuilder {

    private final ModuleType moduleType;

    public DoNotUseModuleBuilder(ModuleType moduleType){
        this.moduleType = moduleType;
    }

    @Override
    public ModuleType<?> getModuleType() {
        return moduleType;
    }

    @Override
    public ModuleWizardStep[] createWizardSteps(@NotNull WizardContext wizardContext, @NotNull ModulesProvider modulesProvider) {
        return new ModuleWizardStep[]{new DoNotUseModuleWizardStep()};
    }

}
