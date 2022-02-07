package io.datakitchen.ide.editors.file;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

import javax.swing.*;
import java.util.Map;

public class AzureBlobDataSourceEditor extends FileDataSourceEditor{

    private AzureBlobConfigEditor configEditor;
    public AzureBlobDataSourceEditor(Project project, VirtualFile file) {
        super(project, file);
    }

    @Override
    protected JComponent buildConfigurationPanel() {
        Module module = ModuleUtil.findModuleForFile(file, project);
        configEditor = new AzureBlobConfigEditor(module);
        return configEditor;
    }

    @Override
    protected void loadConfiguration(Map<String, Object> config) {
        configEditor.loadConfiguration(config);
    }

    @Override
    protected void saveConfiguration(Map<String, Object> config) {
        configEditor.saveConfiguration(config);
    }

    @Override
    protected void disableEvents() {
    }

    @Override
    protected void enableEvents() {
    }

    @Override
    protected void setConfigurationEnabled(boolean enabled) {
        configEditor.setEnabled(enabled);
    }

    @Override
    protected String getDsType() {
        return "DKDataSource_AzureBlob";
    }

}
