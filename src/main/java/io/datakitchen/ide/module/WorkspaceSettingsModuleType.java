package io.datakitchen.ide.module;

import com.intellij.icons.AllIcons;
import com.intellij.ide.util.projectWizard.ModuleBuilder;
import com.intellij.openapi.module.ModuleType;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * This module type is intended to be used for storing local overrides only
 * Not to be used for creating new modules by user
 */
public class WorkspaceSettingsModuleType extends ModuleType {

    public static final String MODULE_TYPE_ID = "dk-workspace-settings";

    public WorkspaceSettingsModuleType() {
        super(MODULE_TYPE_ID);
    }

    @NotNull
    @Override
    public ModuleBuilder createModuleBuilder() {
        return new DoNotUseModuleBuilder(this);
    }

    @Override
    public @NotNull @Nls(capitalization = Nls.Capitalization.Title) String getName() {
        return "Internal - do not use - 2";
    }

    @Override
    public @NotNull @Nls(capitalization = Nls.Capitalization.Sentence) String getDescription() {
        return "Do not use";
    }

    @Override
    public @NotNull Icon getNodeIcon(boolean isOpened) {
        return AllIcons.FileTypes.Any_type;
    }

}
