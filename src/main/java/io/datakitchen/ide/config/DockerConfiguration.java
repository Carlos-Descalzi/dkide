package io.datakitchen.ide.config;

import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;

public class DockerConfiguration implements Serializable {

    private static final long serialVersionUID = 1;

    private String username;
    private String password;
    private String executablePath;
    private String socketPath;

    public DockerConfiguration(){
        executablePath = "/usr/bin/docker";
        socketPath = "/var/run/docker.sock";
    }

    public boolean equals(Object other){
        return other instanceof DockerConfiguration
                && StringUtils.equals(username, ((DockerConfiguration)other).username)
                && StringUtils.equals(password, ((DockerConfiguration)other).password)
                ;
    }

    public DockerConfiguration(String username, String password, String executablePath, String socketPath) {
        this.username = username;
        this.password = password;
        this.executablePath = executablePath;
        this.socketPath = socketPath;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getExecutablePath() {
        return executablePath;
    }

    public void setExecutablePath(String executablePath) {
        this.executablePath = executablePath;
    }

    public String getSocketPath() {
        return socketPath;
    }

    public void setSocketPath(String socketPath) {
        this.socketPath = socketPath;
    }

}
