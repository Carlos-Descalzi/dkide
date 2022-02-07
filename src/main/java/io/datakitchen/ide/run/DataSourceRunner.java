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
import io.datakitchen.ide.tools.DataSourceResultsView;
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

public class DataSourceRunner extends Runner{

    private final VirtualFile sourceFile;

    public DataSourceRunner(Module module, VirtualFile sourceFile) {
        super(module);
        this.sourceFile = sourceFile;
    }

    public void run(){

        Project project = module.getProject();

        ConfigurationService service = ConfigurationService.getInstance(project);
        DockerConfiguration dockerSettings = service.getDockerConfiguration();
        VirtualFile recipeFolder = RecipeUtil.recipeFolder(module);
        VirtualFile localOverrides = RecipeUtil.getLocalOverridesFile(module.getProject());
        String activeVariation = RecipeUtil.getActiveVariation(module);
        try {
            VirtualFile dataFolder = RecipeUtil.createTempFolder(module);

            ContainerDefinition containerDefinition = new ContainerDefinition();
            containerDefinition.setImageName(Constants.RECIPE_RUNNER_IMAGE);

            containerDefinition.setEnvironment(DockerUtil.makeEnvironment(project));

            Map<String,String> mounts = new HashMap<>();
            mounts.put(recipeFolder.getPath(),"/dk/"+recipeFolder.getName());
            mounts.put(dataFolder.getPath(),"/dk/data");
            mounts.put(DockerUtil.getSocketPath(dockerSettings.getSocketPath()),"/var/run/docker.sock");
            mounts.put(localOverrides.getPath(),"/dk/overrides.json");

            containerDefinition.setMounts(mounts);

            String filePath = sourceFile.getParent().getParent().getName()
                    + "/" +sourceFile.getParent().getName()
                    + "/" +sourceFile.getName();

            List<String> commandLine = Arrays.asList("/dk/data_source_run.sh",
                    recipeFolder.getName(),
                    filePath,
                    "/dk/data",
                    activeVariation
            );

            containerDefinition.setCommandLine(commandLine);

            DataSourceResultsView resultsView = new DataSourceResultsView(project,dataFolder);

            runContainer("Run datasource "+sourceFile.getName(),project, containerDefinition, ()->
                        SwingUtilities.invokeLater(()->
                            dataFolder.refresh(true, true, resultsView::refresh)
                        )
                    , getDiskUsageSupplier(dataFolder),
                    new ContentImpl(resultsView,"Output",false));

        } catch (Exception ex){
            Messages.showMessageDialog(project, ex.getMessage(), "Error", Messages.getErrorIcon());
        }
    }

    protected Supplier<Long> getDiskUsageSupplier(VirtualFile path){
        return () -> FileUtils.sizeOfDirectory(new File(path.getPath()));
    }

}
