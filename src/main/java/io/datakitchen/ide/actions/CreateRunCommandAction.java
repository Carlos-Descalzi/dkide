package io.datakitchen.ide.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import io.datakitchen.ide.Constants;
import io.datakitchen.ide.config.ConfigurationService;
import io.datakitchen.ide.config.DockerConfiguration;
import io.datakitchen.ide.util.DockerUtil;
import io.datakitchen.ide.util.RecipeUtil;
import org.jdesktop.swingx.plaf.basic.core.BasicTransferable;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.Transferable;
import java.util.Map;
import java.util.stream.Collectors;

public class CreateRunCommandAction extends AnAction implements ClipboardOwner {


    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {

        Project project = e.getProject();
        Module module = e.getData(LangDataKeys.MODULE);
        VirtualFile recipeFolder = RecipeUtil.recipeFolder(module);

        ConfigurationService service = ConfigurationService.getInstance(project);
        DockerConfiguration dockerSettings = service.getDockerConfiguration();
        VirtualFile localOverrides = RecipeUtil.getLocalOverridesFile(project);
        String activeVariation = RecipeUtil.getActiveVariation(module);

        String builder = "docker run " +
            DockerUtil.makeEnvironment(project)
                .entrySet().stream()
                .map((Map.Entry<String, String> entry) ->
                    String.format("-e %s=\"%s\"",
                        entry.getKey(),
                        String.valueOf(entry.getValue()).replace("\"", "\\\"")
                    )
                ).collect(Collectors.joining(" "))
            + " -v " + recipeFolder.getPath() + ":/dk/" + recipeFolder.getName()
            + " -v " + "output_data:/dk/output_data"
            + " -v " + dockerSettings.getSocketPath() + ":/var/run/docker.sock"
            + " -v " + localOverrides.getPath() + ":/dk/overrides.json"
            + " " + Constants.RECIPE_RUNNER_IMAGE
            + " /dk/recipe_run_local.sh"
            + " " + module.getName()
            + " " + activeVariation
            + " output_data";

        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new BasicTransferable(builder, null), this);
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        super.update(e);
        e.getPresentation().setEnabled(
            e.getProject() != null
            && e.getData(LangDataKeys.MODULE) != null
        );
    }

    @Override
    public void lostOwnership(Clipboard clipboard, Transferable contents) {}
}
