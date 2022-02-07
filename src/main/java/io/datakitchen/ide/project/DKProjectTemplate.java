package io.datakitchen.ide.project;

import com.intellij.ide.util.projectWizard.AbstractModuleBuilder;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.platform.ProjectTemplate;
import io.datakitchen.ide.module.EmptyModuleBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class DKProjectTemplate implements ProjectTemplate {
    @Override
    public @NotNull @NlsContexts.Label String getName() {
        return "DataKitchen Workspace";
    }

    @Override
    public @Nullable @NlsContexts.DetailedDescription String getDescription() {
        return "DataKitchen workspace for recipes";
    }

    @Override
    public Icon getIcon() {
        return new ImageIcon(getClass().getResource("/icons/dk-icon.png"));
    }

    @Override
    public @NotNull AbstractModuleBuilder createModuleBuilder() {
        return new EmptyModuleBuilder();
    }

    @Override
    public @Nullable ValidationInfo validateSettings() {
        return null;
    }


}
