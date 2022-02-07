package io.datakitchen.ide.service;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.InspectContainerResponse;

import java.util.Map;

public class ContainerImpl implements Container{
    private DockerClient client;
    private String containerId;
    private String workDir;

    public ContainerImpl(DockerClient client, String containerId) {
        this.client = client;
        this.containerId = containerId;
    }

    public String getContainerWorkDir(){
        if (workDir == null) {
            InspectContainerResponse response = client.inspectContainerCmd(containerId).exec();
            // getGraphDriver() doesn't work.
            Map<String, Object> graphDriverData = (Map<String, Object>)response.getRawValues().get("GraphDriver");
            Map<String, Object> data = (Map<String, Object>)graphDriverData.get("Data");
            workDir = (String) data.get("UpperDir");
        }
        return workDir;
    }

    public DockerClient getClient() {
        return client;
    }

    public void setClient(DockerClient client) {
        this.client = client;
    }

    public String getContainerId() {
        return containerId;
    }

    public void setContainerId(String containerId) {
        this.containerId = containerId;
    }
}
