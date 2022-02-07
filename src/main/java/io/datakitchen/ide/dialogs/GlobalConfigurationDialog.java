package io.datakitchen.ide.dialogs;

import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.components.JBTabbedPane;
import io.datakitchen.ide.config.GlobalConfiguration;
import io.datakitchen.ide.config.editors.*;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class GlobalConfigurationDialog extends DialogWrapper {

    private final AccountListEditor accountsInfo;
    private final SitesEditor sitesEditor;
    private final DockerConfigurationEditor dockerConfiguration;
    private final SecretListEditor secretListEditor;
    private final SQLConnectionListEditor connections;
    private final MiscOptionsEditor miscOptionsEditor;

    JTabbedPane tabs = new JBTabbedPane();

    public GlobalConfigurationDialog() {
        super(true);
        setTitle("Global Configuration");
        dockerConfiguration = new DockerConfigurationEditor();
        accountsInfo = new AccountListEditor();
        sitesEditor = new SitesEditor();
        secretListEditor = new SecretListEditor();
        connections = new SQLConnectionListEditor();
        miscOptionsEditor = new MiscOptionsEditor();

        tabs.addTab("Docker Settings",dockerConfiguration);
        tabs.addTab("Accounts",accountsInfo);
        tabs.addTab("Sites", sitesEditor);
        tabs.addTab("Secrets",secretListEditor);
        tabs.addTab("SQL Connections",connections);
        tabs.addTab("Miscelaneous",miscOptionsEditor);
        init();
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        return tabs;
    }

    public void setGlobalConfiguration(GlobalConfiguration config){

        dockerConfiguration.setConfiguration(config.getDockerConfiguration());
        sitesEditor.setSites(config.getSites());
        accountsInfo.setLoginInfoList(config.getAccounts());
        secretListEditor.setSecrets(config.getSecrets());
        connections.setConnections(config.getConnections());
        miscOptionsEditor.setMiscOptions(config.getMiscOptions());
    }

    public void saveGlobalConfiguration(GlobalConfiguration config){
        config.setDockerConfiguration(dockerConfiguration.getConfiguration());
        config.setSites(sitesEditor.getSites());
        config.setAccounts(accountsInfo.getLoginInfoList());
        config.setSecrets(secretListEditor.getSecrets());
        config.setConnections(connections.getConnections());
        config.setMiscOptions(miscOptionsEditor.getMiscOptions());
    }

    public boolean shouldPullImages(){
        return dockerConfiguration.shouldPullImages();
    }

    public boolean socketPathChanged(){
        return dockerConfiguration.socketPathChanged();
    }
}
