package io.datakitchen.ide.project;

import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.openapi.util.IconLoader;
import com.intellij.platform.ProjectTemplate;
import com.intellij.platform.ProjectTemplatesFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class DkProjectTemplateFactory extends ProjectTemplatesFactory {
    public DkProjectTemplateFactory() {
    }

    @Override
    public String @NotNull [] getGroups() {
        return new String[]{"DataKitchen"};
    }

    public Icon getGroupIcon(String group) {
        return IconLoader.findIcon("/icons/dk-icon.png",getClass());
    }
    @Override
    public ProjectTemplate @NotNull [] createTemplates(@Nullable String group, WizardContext context) {
        return new ProjectTemplate[]{new DKProjectTemplate()};
    }

}
