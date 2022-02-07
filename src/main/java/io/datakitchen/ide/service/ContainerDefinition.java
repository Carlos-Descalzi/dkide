package io.datakitchen.ide.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ContainerDefinition {

    private String imageName;
    private Map<String,String> mounts = new HashMap<>();
    private Map<String, String> environment = new HashMap<>();
    private List<String> commandLine;
    private String containerName;
    private String userName;
    private String password;
    private String registryUrl;

    public ContainerDefinition() {
    }

    public ContainerDefinition(String imageName, Map<String,String> mounts, Map<String, String> environment, List<String> commandLine) {
        this.imageName = imageName;
        this.mounts = mounts;
        this.environment = environment;
        this.commandLine = commandLine;
    }

    public ContainerDefinition(String imageName, List<String> commandLine) {
        this.imageName = imageName;
        this.commandLine = commandLine;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public Map<String,String> getMounts() {
        return mounts;
    }

    public void setMounts(Map<String,String> mounts) {
        this.mounts = mounts;
    }

    public Map<String, String> getEnvironment() {
        return environment;
    }

    public void setEnvironment(Map<String, String> environment) {
        this.environment = environment;
    }

    public List<String> getCommandLine() {
        return commandLine;
    }

    public void setCommandLine(List<String> commandLine) {
        this.commandLine = commandLine;
    }

    public String getContainerName() {
        return containerName;
    }

    public void setContainerName(String containerName) {
        this.containerName = containerName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRegistryUrl() {
        return registryUrl;
    }

    public void setRegistryUrl(String registryUrl) {
        this.registryUrl = registryUrl;
    }
}
