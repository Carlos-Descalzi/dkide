package io.datakitchen.ide.editors.sql;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

import javax.swing.*;
import java.util.Map;

public class PostgreSQLDataSourceEditor extends SqlDataSourceEditor {


    private PostgreSQLConfigEditor configEditor;

    public PostgreSQLDataSourceEditor(Project project, VirtualFile file) {
        super(project, file);
    }

    protected String getDsType() {
        return "DKDataSource_PostgreSQL";
    }
    @Override
    protected JComponent buildConfigurationPanel() {
        Module module = ModuleUtil.findModuleForFile(file, project);
        configEditor = new PostgreSQLConfigEditor(module);
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

    protected void setConfigurationEnabled(boolean enabled){
        configEditor.setEnabled(enabled);
    }

    @Override
    protected void disableEvents() {

    }

    @Override
    protected void enableEvents() {

    }

    protected SQLSourceKeyEditor buildKeyEditor(){
        Module module = ModuleUtil.findModuleForFile(file, project);
        return new SQLSourceKeyEditor(module, new String[]{"freebcp"});
    }

}
