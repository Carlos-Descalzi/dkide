package io.datakitchen.ide.editors;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import io.datakitchen.ide.run.NodeRunner;
import io.datakitchen.ide.ui.SimpleAction;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public abstract class AbstractNodeEditor extends RecipeElementEditor{
    public AbstractNodeEditor(Project project, VirtualFile file) {
        super(project, file);
    }

    protected void doShow(){
        super.doShow();
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JButton(new SimpleAction(AllIcons.Actions.Execute, "Run Node", "Run Node", this::runNode)));
        contentPane.add(topPanel, BorderLayout.NORTH);
    }

    private void runNode(ActionEvent event) {
        Module module = ModuleUtil.findModuleForFile(file, project);
        new NodeRunner(module, file.getParent()).run();
    }

//    protected void switchToSourceView(ActionEvent event) {
//        RecipeUtil.switchNodeTo
//    }
}
