package io.datakitchen.ide.run;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.content.impl.ContentImpl;
import io.datakitchen.ide.Constants;
import io.datakitchen.ide.config.ConfigurationService;
import io.datakitchen.ide.config.DockerConfiguration;
import io.datakitchen.ide.service.ContainerDefinition;
import io.datakitchen.ide.tools.RecipeResultsView;
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

public class VariationRunner extends Runner{

    private static final Logger LOGGER = Logger.getInstance(VariationRunner.class);

    private String variation;

    public VariationRunner(Module module) {
        super(module);
    }

    public VariationRunner setVariation(String variation){
        this.variation = variation;
        return this;
    }

    @Override
    public void run() {
        Project project = module.getProject();
        VirtualFile recipeFolder = RecipeUtil.recipeFolder(module);
        String activeVariation = variation != null ? variation : RecipeUtil.getActiveVariation(module);

        try {
            VirtualFile dataFolder = RecipeUtil.createTempFolder(module);
            VirtualFile localOverrides = RecipeUtil.getLocalOverridesFile(module.getProject());
            ConfigurationService service = ConfigurationService.getInstance(project);
            DockerConfiguration dockerSettings = service.getDockerConfiguration();

            ContainerDefinition containerDefinition = new ContainerDefinition();
            containerDefinition.setImageName(Constants.RECIPE_RUNNER_IMAGE);

            containerDefinition.setEnvironment(DockerUtil.makeEnvironment(project));

            Map<String,String> mounts = new HashMap<>();
            mounts.put(recipeFolder.getPath(),"/dk/"+recipeFolder.getName());
            mounts.put(dataFolder.getPath(),"/dk/data");
            mounts.put(DockerUtil.getSocketPath(dockerSettings.getSocketPath()),"/var/run/docker.sock");
            mounts.put(localOverrides.getPath(),"/dk/overrides.json");

            containerDefinition.setMounts(mounts);


            List<String> commandLine = Arrays.asList("/dk/recipe_run_local.sh",
                    recipeFolder.getName(),
                    activeVariation,
                    "/dk/data"
            );

            containerDefinition.setCommandLine(commandLine);

            RecipeResultsView resultsView = new RecipeResultsView(project,recipeFolder,dataFolder);

            runContainer("Run variation "+ activeVariation, project, containerDefinition, ()->
                            SwingUtilities.invokeLater(()->
                                    dataFolder.refresh(true, true, resultsView::refresh)
                            )
                    , getDiskUsageSupplier(dataFolder),
                    new ContentImpl(resultsView,"Output",false));
        } catch (Exception ex){
            ex.printStackTrace();
        }
    }

    protected Supplier<Long> getDiskUsageSupplier(VirtualFile path){
        return () -> FileUtils.sizeOfDirectory(new File(path.getPath()));

    }

}
