package io.datakitchen.ide.editors;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import io.datakitchen.ide.run.DataSourceRunner;
import io.datakitchen.ide.ui.SimpleAction;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public abstract class AbstractDataSourceEditor extends RecipeElementEditor{
    public AbstractDataSourceEditor(Project project, VirtualFile file) {
        super(project, file);
    }

    protected void doShow(){
        super.doShow();
        if (showRunAction()) {
            JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            topPanel.add(new JButton(new SimpleAction(AllIcons.Actions.Execute, "Run Data Source", "Run Data Source", this::runDataSource)));
            contentPane.add(topPanel, BorderLayout.NORTH);
        }
    }

    private void runDataSource(ActionEvent event) {
        Module module = ModuleUtil.findModuleForFile(file, project);
        new DataSourceRunner(module, file).run();
    }

    protected boolean showRunAction(){
        return true;
    }
}
