package io.datakitchen.ide.editors.sql;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

import javax.swing.*;
import java.util.Map;

public class BigQueryDataSourceEditor extends SqlDataSourceEditor{

    private BigQueryConfigEditor configEditor;

    public BigQueryDataSourceEditor(Project project, VirtualFile file) {
        super(project, file);
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
        return "DKDataSource_BigQuery";
    }

    @Override
    protected JComponent buildConfigurationPanel() {
        Module module = ModuleUtil.findModuleForFile(file, project);
        configEditor = new BigQueryConfigEditor(module);
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
}
