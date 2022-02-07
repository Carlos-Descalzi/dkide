package io.datakitchen.ide.editors.file;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

import javax.swing.*;
import java.util.Map;

public class S3DataSinkEditor extends FileDataSinkEditor{


    private S3ConfigEditor configEditor;

    public S3DataSinkEditor(Project project, VirtualFile file) {
        super(project, file);
    }

    @Override
    protected JComponent buildConfigurationPanel() {
        Module module = ModuleUtil.findModuleForFile(file, project);
        configEditor = new S3ConfigEditor(module);
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
        return "DKDataSink_S3";
    }

}
