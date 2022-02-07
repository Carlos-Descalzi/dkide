package io.datakitchen.ide.dialogs;

import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.components.JBTabbedPane;
import io.datakitchen.ide.config.Account;
import io.datakitchen.ide.config.DockerConfiguration;
import io.datakitchen.ide.config.ProjectConfiguration;
import io.datakitchen.ide.config.Secret;
import io.datakitchen.ide.config.editors.*;
import io.datakitchen.ide.tools.DatabaseConfiguration;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class ProjectConfigurationDialog extends DialogWrapper {

    private final AccountListEditor accountsInfo;
    private final DockerConfigurationEditor dockerConfiguration;
    private final SecretListEditor secretListEditor;
    private final SQLConnectionListEditor connections;
    private final ProjectSettingsEditor projectSettingsEditor;

    private final JCheckBox useGlobalAccounts = new JCheckBox("Use global account settings");
    private final JCheckBox useGlobalDockerConfig = new JCheckBox("Use global docker settings");
    private final JCheckBox useGlobalSecrets = new JCheckBox("Use global secrets");
    private final JCheckBox useGlobalConnections = new JCheckBox("Use global connection settings");

    private final JTabbedPane tabs = new JBTabbedPane();

    public ProjectConfigurationDialog() {
        super(true);
        setTitle("Project Configuration");

        dockerConfiguration = new DockerConfigurationEditor();
        accountsInfo = new AccountListEditor();
        secretListEditor = new SecretListEditor();
        connections = new SQLConnectionListEditor();
        projectSettingsEditor = new ProjectSettingsEditor();

        JPanel dockerConfigurationPanel = new JPanel(new BorderLayout());
        dockerConfigurationPanel.add(useGlobalDockerConfig, BorderLayout.NORTH);
        dockerConfigurationPanel.add(dockerConfiguration, BorderLayout.CENTER);

        tabs.addTab("Docker Settings",dockerConfigurationPanel);

        JPanel accountsPanel = new JPanel(new BorderLayout());
        accountsPanel.add(useGlobalAccounts, BorderLayout.NORTH);
        accountsPanel.add(accountsInfo, BorderLayout.CENTER);

        tabs.addTab("Accounts",accountsPanel);

        JPanel secretsPanel = new JPanel(new BorderLayout());
        secretsPanel.add(useGlobalSecrets, BorderLayout.NORTH);
        secretsPanel.add(secretListEditor, BorderLayout.CENTER);

        tabs.addTab("Secrets",secretsPanel);

        JPanel connectionsPanel = new JPanel(new BorderLayout());
        connectionsPanel.add(useGlobalConnections, BorderLayout.NORTH);
        connectionsPanel.add(connections, BorderLayout.CENTER);

        tabs.addTab("SQL Connections",connectionsPanel);
        tabs.addTab("Project settings", projectSettingsEditor);

        useGlobalDockerConfig.addChangeListener(e -> updateState());
        useGlobalAccounts.addChangeListener(e -> updateState());
        useGlobalSecrets.addChangeListener(e -> updateState());
        useGlobalConnections.addChangeListener(e -> updateState());

        updateState();

        init();
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        return tabs;
    }

    private void updateState(){
        dockerConfiguration.setEnabled(!useGlobalDockerConfig.isSelected());
        accountsInfo.setEnabled(!useGlobalAccounts.isSelected());
        secretListEditor.setEnabled(!useGlobalSecrets.isSelected());
        connections.setEnabled(!useGlobalConnections.isSelected());
    }

    public void setProjectConfiguration(ProjectConfiguration config){
        DockerConfiguration dockerConfiguration = config.getDockerConfiguration();
        useGlobalDockerConfig.setSelected(dockerConfiguration == null);
        this.dockerConfiguration.setConfiguration(dockerConfiguration);

        List<Account> accounts = config.getAccounts();
        useGlobalAccounts.setSelected(accounts == null);
        this.accountsInfo.setLoginInfoList(accounts);

        List<Secret> secrets = config.getSecrets();
        useGlobalSecrets.setSelected(secrets == null);
        this.secretListEditor.setSecrets(secrets);

        List<DatabaseConfiguration> connections = config.getConnections();
        useGlobalConnections.setSelected(connections == null);
        this.connections.setConnections(connections);

        this.projectSettingsEditor.setProjectSettings(config.getProjectSettings());
    }

    public void saveProjectConfiguration(ProjectConfiguration config){
        config.setDockerConfiguration(
                useGlobalDockerConfig.isSelected()
                ? null
                : config.getDockerConfiguration());
        config.setAccounts(
                useGlobalAccounts.isSelected()
                ? null
                : accountsInfo.getLoginInfoList());
        config.setSecrets(
                useGlobalSecrets.isSelected()
                ? null
                : secretListEditor.getSecrets());
        config.setConnections(
                useGlobalConnections.isSelected()
                ? null
                : connections.getConnections());

        config.setProjectSettings(projectSettingsEditor.getProjectSettings());
    }

}
