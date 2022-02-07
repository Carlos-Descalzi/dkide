package io.datakitchen.ide.module;

import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.openapi.options.ConfigurationException;

import javax.swing.*;
import javax.swing.text.html.HTMLEditorKit;

public class DoNotUseModuleWizardStep extends ModuleWizardStep {
    @Override
    public JComponent getComponent() {

        JTextPane pane = new JTextPane();
        pane.setEditorKit(new HTMLEditorKit());
        pane.setText(
            "<html><body><center>"
            +"<h1>Do not use this option</h1>"
            +"<p></p>"
            +"<p></p>"
            +"<p></p>"
            +"This module is not intended to be created directly.<br/>"
            +"Create a workspace by going to <b>New Project -> DataKitchen -> DataKitchen Workspace</b><br/>"
            +"Once the workspace is created, go to <b>New -> Add Recipe to Project</b> option"
            +"</center></body></html>"
        );

        return pane;
    }

    @Override
    public void updateDataModel() {
    }

    @Override
    public boolean validate() throws ConfigurationException {
        return false;
    }
}
