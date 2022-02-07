package io.datakitchen.ide.run;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.content.impl.ContentImpl;
import io.datakitchen.ide.Constants;
import io.datakitchen.ide.config.ConfigurationService;
import io.datakitchen.ide.config.DockerConfiguration;
import io.datakitchen.ide.service.ContainerDefinition;
import io.datakitchen.ide.tools.NodeResultsView;
import io.datakitchen.ide.util.DockerUtil;
import io.datakitchen.ide.util.RecipeUtil;
import org.apache.commons.io.FileUtils;

import javax.swing.*;
import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class NodeRunner extends Runner{

    private final VirtualFile nodePath;

    public NodeRunner(Module module, VirtualFile nodePath) {
        super(module);
        this.nodePath = nodePath;
    }

    @Override
    public void run() {
        Project project = module.getProject();
        VirtualFile nodePath = this.nodePath;

        if (nodePath.getName().equals(Constants.FILE_NOTEBOOK_JSON)){
            nodePath = nodePath.getParent();
        }

        VirtualFile recipeFolder = RecipeUtil.recipeFolder(module);
        String activeVariation = RecipeUtil.getActiveVariation(module);
        if (activeVariation == null){
            Messages.showMessageDialog(project,
                    "Active variation not set. To set the active variation right click on the recipe and "
                            +"go to \"Set Active Variation\"",
                    "Error",
                    Messages.getErrorIcon()
            );
            return;
        }
        try {
            VirtualFile dataFolder = RecipeUtil.createTempFolder(module);
            VirtualFile localOverrides = RecipeUtil.getLocalOverridesFile(project);
            ConfigurationService service = ConfigurationService.getInstance(project);
            DockerConfiguration dockerSettings = service.getDockerConfiguration();

            ContainerDefinition containerDefinition = new ContainerDefinition();
            containerDefinition.setImageName(Constants.RECIPE_RUNNER_IMAGE);

            containerDefinition.setEnvironment(DockerUtil.makeEnvironment(project));

            Map<String,String> mounts = new HashMap<>();
            mounts.put(recipeFolder.getPath(),"/dk/"+recipeFolder.getName());
            mounts.put(dataFolder.getPath(),"/dk/data");

            String dockerSocket = dockerSettings.getSocketPath();

            mounts.put(DockerUtil.getSocketPath(dockerSocket),"/var/run/docker.sock");
            mounts.put(localOverrides.getPath(),"/dk/overrides.json");

            containerDefinition.setMounts(mounts);

            List<String> commandLine = Arrays.asList("/dk/node_run.sh",
                    recipeFolder.getName(),
                    nodePath.getName(),
                    "/dk/data",
                    activeVariation
            );

            containerDefinition.setCommandLine(commandLine);

            NodeResultsView resultsView = new NodeResultsView(project,nodePath,dataFolder);

            runContainer("Run node "+nodePath.getName(),project, containerDefinition, ()->
                        SwingUtilities.invokeLater(()->
                            dataFolder.refresh(true, true, resultsView::refresh)
                        )
                    ,
                    getDiskUsageSupplier(dataFolder),
                    new ContentImpl(resultsView,"Output",false)
            );

        }catch (Exception ex){
            Messages.showMessageDialog(project, ex.getMessage(), "Error", Messages.getErrorIcon());
        }
    }

    protected Supplier<Long> getDiskUsageSupplier(VirtualFile path){
        return () -> FileUtils.sizeOfDirectory(new File(path.getPath()));

    }
}
