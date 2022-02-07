package io.datakitchen.ide.actions;

import com.intellij.notification.Notification;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.content.impl.ContentImpl;
import io.datakitchen.ide.Constants;
import io.datakitchen.ide.config.Secret;
import io.datakitchen.ide.model.NodeType;
import io.datakitchen.ide.service.CompilerService;
import io.datakitchen.ide.service.ContainerDefinition;
import io.datakitchen.ide.service.ContainerService;
import io.datakitchen.ide.tools.ScriptResultsView;
import io.datakitchen.ide.ui.UIUtil;
import io.datakitchen.ide.util.JsonUtil;
import io.datakitchen.ide.util.RecipeUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.tools.ant.types.Commandline;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RunContainerAction extends AbstractContainerRunAction {

    private static final Logger LOGGER = Logger.getInstance(RunContainerAction.class);

    @Override
    protected void doRunAction(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        Module module = e.getData(LangDataKeys.MODULE);
        VirtualFile nodeFolder = e.getData(LangDataKeys.VIRTUAL_FILE);

        if (project == null){
            LOGGER.error("Project was null");
            return;
        }
        if (module == null){
            LOGGER.error("Module was null");
            return;
        }
        if (nodeFolder == null){
            LOGGER.error("node folder was null");
            return;
        }

        VirtualFile notebookJson = nodeFolder.findChild(Constants.FILE_NOTEBOOK_JSON);

        Notification msg = UIUtil.showNotificationSync(
                project,
                "Pulling image, please wait ...",
                ""
        );
        VirtualFile dockerShareFolder = nodeFolder.findChild("docker-share");

        if (dockerShareFolder == null){
            Messages.showErrorDialog("Unable to run container, docker-share folder does not exist", "Error");
            return;
        }

        CompilerService.getInstance(module).compileFile(notebookJson, (String notebook)->{
            try {
                Map<String, Object> notebookData = JsonUtil.read(notebook);
                VirtualFile testsFolder = nodeFolder.findChild("test-files");
                VirtualFile outputFolder = RecipeUtil.createTempFolder(module);

                ContainerDefinition containerDefinition = buildContainerDefinition(notebookData);

                ContainerService.getInstance(project).pullAsync(
                    containerDefinition,
                    (image)->{
                        SwingUtilities.invokeLater(msg::hideBalloon);

                        String workDir = image.getContainerConfig().getWorkingDir();

                        containerDefinition.setMounts(buildMounts(dockerShareFolder, testsFolder, outputFolder, workDir));
                        containerDefinition.setEnvironment(buildEnvironment(project, workDir));
                        ScriptResultsView resultsView = new ScriptResultsView(project,nodeFolder,outputFolder);
                        runContainer("Run container "+containerDefinition.getImageName(),project, containerDefinition, ()->{
                                    SwingUtilities.invokeLater(()->{
                                        outputFolder.refresh(true, true, resultsView::refresh);
                                    });
                                }, getDiskUsageSupplier(outputFolder),
                                new ContentImpl(resultsView,"Output",false));

                    },
                    (error)->{
                        SwingUtilities.invokeLater(()->{
                            msg.hideBalloon();
                            Messages.showMessageDialog(project, error, "Error", Messages.getErrorIcon());
                        });

                    }
                );

            }catch (Exception ex){
                SwingUtilities.invokeLater(()->
                    Messages.showMessageDialog(project, String.valueOf(ex.getMessage()), "Error", Messages.getErrorIcon())
                );
            }

        });
    }

    private Map<String, String> buildMounts(
            VirtualFile dockerShareFolder,
            VirtualFile testsFolder,
            VirtualFile outputFolder,
            String workDir) {

        Map<String, String> mounts = new HashMap<>();

        String containerDockerShareFolder = workDir+ "/"+ Constants.DOCKER_SHARE_FOLDER_NAME;

        mounts.put(outputFolder.getPath(), containerDockerShareFolder);

        for (VirtualFile dockerShareItem: dockerShareFolder.getChildren()){
            mounts.put(dockerShareItem.getPath(),containerDockerShareFolder + "/" + dockerShareItem.getName());
        }

        if (testsFolder != null){
            for (VirtualFile testFile: testsFolder.getChildren()){
                mounts.put(testFile.getPath(),containerDockerShareFolder + "/" + testFile.getName());
            }
        }

        return mounts;
    }

    protected Map<String, String> buildEnvironment(Project project, String workDir) {

        Map<String, String> environment = new HashMap<>();

        String containerDockerShareFolder = workDir+ "/"+ Constants.DOCKER_SHARE_FOLDER_NAME;

        environment.put("INSIDE_CONTAINER_FILE_MOUNT", workDir);
        environment.put("INSIDE_CONTAINER_FILE_DIRECTORY",Constants.DOCKER_SHARE_FOLDER_NAME);
        environment.put("CONTAINER_INPUT_CONFIG_FILE_PATH",containerDockerShareFolder + "/config.json");
        environment.put("CONTAINER_INPUT_CONFIG_FILE_NAME","config.json");
        environment.put("CONTAINER_OUTPUT_PROGRESS_FILE","ac_progress.json");
        environment.put("CONTAINER_OUTPUT_LOG_FILE","ac_logger.log");

        List<Secret> secrets = getSecrets(project);
        if (secrets != null) {
            for (Secret secret : secrets) {
                String envKey = secret.getPath().replace("/", "_").replace("-","_").toUpperCase();
                environment.put(envKey, secret.getValue());
            }
        }

        return environment;
    }

    private ContainerDefinition buildContainerDefinition(
            Map<String, Object> notebookData) {

        ContainerDefinition containerDefinition = new ContainerDefinition();

        String imageName = (String)notebookData.get("image-repo");
        String imageTag = (String)notebookData.get("image-tag");
        String namespace = (String)notebookData.get("dockerhub-namespace");

        String image = (StringUtils.isNotBlank(namespace) ? namespace+"/" : "")
                + imageName
                + ":"
                + (StringUtils.isBlank(imageTag) ? "latest" : imageTag);

        containerDefinition.setImageName(image);
        containerDefinition.setRegistryUrl((String)notebookData.get("dockerhub-url"));
        containerDefinition.setUserName((String)notebookData.get("dockerhub-username"));
        containerDefinition.setPassword((String)notebookData.get("dockerhub-password"));

        String commandLineString = (String)notebookData.get("command-line");

        if (commandLineString != null) {
            containerDefinition.setCommandLine(Arrays.asList(Commandline.translateCommandline(commandLineString)));
        }
        return containerDefinition;
    }


    @Override
    public void update(@NotNull AnActionEvent e) {
        VirtualFile path = e.getData(LangDataKeys.VIRTUAL_FILE);
        Module module = e.getData(LangDataKeys.MODULE);
        String activeVariation = RecipeUtil.getActiveVariation(module);

        boolean enabled;
        try {
            enabled = path != null
                    && activeVariation != null
                    && RecipeUtil.isNodeFolder(module, path)
                    && RecipeUtil.getNodeType(path).equals(NodeType.CONTAINER_NODE.getTypeName());
        }catch (Exception ex){
            enabled = false;
        }
        e.getPresentation().setEnabled(enabled);
    }
}
