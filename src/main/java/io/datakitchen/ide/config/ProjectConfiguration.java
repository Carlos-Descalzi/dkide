package io.datakitchen.ide.config;

public class ProjectConfiguration extends Configuration{

    private ProjectSettings projectSettings = new ProjectSettings();

    public ProjectSettings getProjectSettings() {
        return projectSettings;
    }

    public void setProjectSettings(ProjectSettings projectSettings) {
        this.projectSettings = projectSettings;
    }
}
