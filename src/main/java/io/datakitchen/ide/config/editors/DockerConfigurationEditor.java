package io.datakitchen.ide.config.editors;

import com.intellij.util.ui.JBUI;
import io.datakitchen.ide.config.DockerConfiguration;
import io.datakitchen.ide.service.ContainerService;
import io.datakitchen.ide.ui.FormPanel;
import io.datakitchen.ide.ui.SimpleAction;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;

public class DockerConfigurationEditor extends JPanel {

    private final JTextField username = new JTextField();
    private final JPasswordField password = new JPasswordField();
    private final JCheckBox pullImages = new JCheckBox();
    private final JTextField socketPath = new JTextField();

    private String originalSocketPath;

    public void setEnabled(boolean enabled){
        super.setEnabled(enabled);
        username.setEnabled(enabled);
        password.setEnabled(enabled);
        pullImages.setEnabled(enabled);
        socketPath.setEnabled(enabled);
    }

    public DockerConfigurationEditor(){

        FormPanel accountPanel = new FormPanel(new Dimension(200,28),new Dimension(200,28));
        accountPanel.setBorder(new CompoundBorder(JBUI.Borders.empty(5), new TitledBorder("Login")));
        accountPanel.addField("Username",username);
        accountPanel.addField("Password",password);
        accountPanel.addField("Pull images when closing",pullImages);

        FormPanel setupPanel = new FormPanel(new Dimension(200,28),new Dimension(200,28));
        setupPanel.setBorder(new CompoundBorder(JBUI.Borders.empty(5), new TitledBorder("Settings")));

        JPanel socketPanel = new JPanel(new BorderLayout());
        socketPanel.add(socketPath, BorderLayout.CENTER);
        socketPanel.add(new JButton(new SimpleAction("Restore default", this::restoreSocket)), BorderLayout.EAST);

        setupPanel.addField("Docker endpoint",socketPanel, new Dimension(400,28));

        setLayout(new GridLayout(2,1));
        add(accountPanel);
        add(setupPanel);
    }

    private void restoreSocket(ActionEvent event) {
        socketPath.setText(ContainerService.getDefaultDockerHost());
    }

    public DockerConfiguration getConfiguration() {
        return new DockerConfiguration(
                username.getText(),
                String.valueOf(password.getPassword()),
                null,
                socketPath.getText());
    }

    public void setConfiguration(DockerConfiguration config) {
        if (config != null) {
            username.setText(StringUtils.defaultString(config.getUsername(), ""));
            password.setText(StringUtils.defaultString(config.getPassword(), ""));

            originalSocketPath = config.getSocketPath();

            socketPath.setText(StringUtils.defaultString(originalSocketPath));
        } else {
            socketPath.setText(ContainerService.getDefaultDockerHost());
        }
    }

    public boolean socketPathChanged(){
        return StringUtils.equals(originalSocketPath, socketPath.getText());
    }

    public boolean shouldPullImages(){
        return pullImages.isSelected();
    }
}
