package io.datakitchen.ide.dialogs;

import com.intellij.openapi.ui.DialogWrapper;
import io.datakitchen.ide.config.GlobalConfiguration;
import io.datakitchen.ide.config.editors.DockerConfigurationEditor;
import io.datakitchen.ide.config.editors.EditionModeEditor;
import io.datakitchen.ide.ui.ButtonsBar;
import io.datakitchen.ide.ui.SimpleAction;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class FirstTimeSetupDialog extends DialogWrapper {

    private final CardLayout cards = new CardLayout();
    private final JPanel mainPanel = new JPanel(cards);
    private final EditionModeEditor editionModeEditor = new EditionModeEditor();
    private final DockerConfigurationEditor dockerConfigurationEditor = new DockerConfigurationEditor();
    private final Action backAction = new SimpleAction("Back",this::goBack);
    private final Action nextAction = new SimpleAction("Next",this::goNext);

    public FirstTimeSetupDialog(){
        super(true);
        setTitle("First Time Setup");
        mainPanel.add(dockerConfigurationEditor, "docker");
        mainPanel.add(editionModeEditor, "mode");
        updateActions();
        init();
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(mainPanel, BorderLayout.CENTER);
        panel.add(new ButtonsBar(backAction, nextAction), BorderLayout.SOUTH);
        return panel;
    }

    private void goBack(ActionEvent event) {
        cards.show(mainPanel,"docker");
        updateActions();
    }

    private void goNext(ActionEvent event) {
        cards.show(mainPanel,"mode");
        updateActions();
    }

    private void updateActions() {
        backAction.setEnabled(!dockerConfigurationEditor.isVisible());
        nextAction.setEnabled(dockerConfigurationEditor.isVisible());
    }

    public void setGlobalConfiguration(GlobalConfiguration config) {
        dockerConfigurationEditor.setConfiguration(config.getDockerConfiguration());
        editionModeEditor.setConfiguration(config.getMiscOptions());
    }

    public void saveGlobalConfiguration(GlobalConfiguration config) {
        config.setDockerConfiguration(dockerConfigurationEditor.getConfiguration());
        config.setMiscOptions(editionModeEditor.getConfiguration());
    }
}
