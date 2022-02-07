package io.datakitchen.ide.config;

import io.datakitchen.ide.tools.DatabaseConfiguration;

import java.io.Serializable;
import java.util.List;

public class Configuration implements Serializable {
    private static final long serialVersionUID = 1;

    private DockerConfiguration dockerConfiguration = null;
    private List<Secret> secrets = null;
    private List<Account> accounts = null;
    private List<DatabaseConfiguration> connections = null;

    public DockerConfiguration getDockerConfiguration() {
        return dockerConfiguration;
    }

    public void setDockerConfiguration(DockerConfiguration dockerConfiguration) {
        this.dockerConfiguration = dockerConfiguration;
    }

    public List<Secret> getSecrets() {
        return secrets;
    }

    public void setSecrets(List<Secret> secrets) {
        this.secrets = secrets;
    }

    public List<Account> getAccounts() {
        return accounts;
    }

    public void setAccounts(List<Account> accounts) {
        this.accounts = accounts;
    }

    public List<DatabaseConfiguration> getConnections() {
        return connections;
    }

    public void setConnections(List<DatabaseConfiguration> connections) {
        this.connections = connections;
    }

}
