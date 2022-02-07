package io.datakitchen.ide.config;

import com.intellij.ide.projectView.ProjectView;
import com.intellij.openapi.project.Project;
import io.datakitchen.ide.Constants;
import io.datakitchen.ide.tools.DatabaseConfiguration;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ConfigurationService {

    private static final String CONFIG_FILE = "dkide.config";

    private final Project project;
    private GlobalConfiguration globalConfiguration;
    private ProjectConfiguration projectConfiguration;

    public ConfigurationService(Project project){
        this.project = project;
        loadGlobalConfiguration();
        loadProjectConfiguration();
    }

    public static ConfigurationService getInstance(Project project){
        return project.getService(ConfigurationService.class);
    }

    private void loadProjectConfiguration() {
        if (project.getProjectFile() != null) {
            projectConfiguration = (ProjectConfiguration) loadConfiguration(new File(project.getProjectFile().getParent().getPath()));
        }
        if (projectConfiguration == null) {
            projectConfiguration = new ProjectConfiguration();
        }
    }

    private void loadGlobalConfiguration() {

        File configFolder = Constants.USER_CONFIG_FOLDER;

        if (configFolder.exists()){
            globalConfiguration = (GlobalConfiguration) loadConfiguration(configFolder);
        }
        if (globalConfiguration == null){
            globalConfiguration = new GlobalConfiguration();
        }
    }

    private Object loadConfiguration(File configFolder) {
        File configFile = new File(configFolder, CONFIG_FILE);
        if (configFile.exists()){
            try (InputStream input = new FileInputStream(configFile)){
                return new ObjectInputStream(input).readObject();
            }catch(Exception ex){
                ex.printStackTrace();
            }
        }
        return null;
    }

    public void saveProjectConfiguration(){
        saveConfiguration(projectConfiguration, new File(project.getProjectFile().getParent().getPath()));
        ProjectView.getInstance(project).refresh();
    }

    public void saveGlobalConfiguration(){

        File configFolder = Constants.USER_CONFIG_FOLDER;

        if (!configFolder.exists()){
            try {
                configFolder.mkdirs();
            }catch (Exception ex){
                ex.printStackTrace();
                return;
            }
        }

        saveConfiguration(globalConfiguration, configFolder);
    }

    private void saveConfiguration(Configuration config, File targetFolder){

        File configFile = new File(targetFolder, CONFIG_FILE);
        try (OutputStream out = new FileOutputStream(configFile)){
            ObjectOutputStream objectOutput = new ObjectOutputStream(out);
            objectOutput.writeObject(config);
        } catch (IOException ex){
            ex.printStackTrace();
        }
    }

    public GlobalConfiguration getGlobalConfiguration() {
        return globalConfiguration;
    }

    public void setGlobalConfiguration(GlobalConfiguration globalConfiguration) {
        this.globalConfiguration = globalConfiguration;
        saveGlobalConfiguration();
    }

    public ProjectConfiguration getProjectConfiguration() {
        return projectConfiguration;
    }

    public void setProjectConfiguration(ProjectConfiguration projectConfiguration) {
        this.projectConfiguration = projectConfiguration;
        saveProjectConfiguration();
    }


    public List<DatabaseConfiguration> getConnections() {
        List<DatabaseConfiguration> connections = projectConfiguration.getConnections();

        if (connections == null){
            connections = globalConfiguration.getConnections();
        }
        if (connections == null){
            connections = new ArrayList<>();
        }
        return connections;
    }

    public List<Secret> getSecrets() {
        List<Secret> secrets = projectConfiguration.getSecrets();
        if (secrets == null){
            secrets = globalConfiguration.getSecrets();
        }
        if (secrets == null){
            secrets = new ArrayList<>();
        }
        return secrets;
    }

    public DockerConfiguration getDockerConfiguration() {
        DockerConfiguration dockerConfiguration = projectConfiguration.getDockerConfiguration();
        if (dockerConfiguration == null){
            dockerConfiguration = globalConfiguration.getDockerConfiguration();
        }
        return dockerConfiguration;
    }

    public boolean globalConfigurationExists() {

        File configFolder = Constants.USER_CONFIG_FOLDER;

        if (!configFolder.exists()){
            return false;
        }

        return new File(configFolder,CONFIG_FILE).exists();

    }
}
