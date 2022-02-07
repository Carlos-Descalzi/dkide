package io.datakitchen.ide.run;

import com.intellij.ide.plugins.PluginManager;
import com.intellij.notification.Notification;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.xdebugger.XDebugProcessStarter;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.XDebuggerManager;
import io.datakitchen.ide.Constants;
import io.datakitchen.ide.debugger.ContainerProcessHandler;
import io.datakitchen.ide.debugger.DebugProcessStarter;
import io.datakitchen.ide.service.Container;
import io.datakitchen.ide.service.ContainerDefinition;
import io.datakitchen.ide.service.ContainerService;
import io.datakitchen.ide.service.ContainerServiceException;
import io.datakitchen.ide.tools.ScriptResultsView;
import io.datakitchen.ide.ui.UIUtil;
import io.datakitchen.ide.util.RecipeUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class ScriptDebugger extends ScriptRunner{

    public ScriptDebugger(Module module, VirtualFile sourceFile) {
        super(module, sourceFile);
    }

    public static boolean isDebugEnabled(){
        PluginId pythonPluginId = PluginId.findId(Constants.PYTHON_PLUGIN_ID);

        return pythonPluginId != null
                && PluginManager.getInstance().findEnabledPlugin(pythonPluginId) != null;
    }

    protected Map<String, String> buildEnvironment(Project project, String secretPrefix) {
        Map<String, String> environment = super.buildEnvironment(project, secretPrefix);

        environment.put("DEBUGGER_HOST", getHostAddress());
        environment.put("DEBUGGER_PORT","59977");

        return environment;
    }

    private String getHostAddress() {
        return "172.17.0.1"; // TODO get this address from docker service.
    }

    @Override
    protected void doRunScript(Module module, VirtualFile scriptFile, File configJsonFile){
        try {
            VirtualFile testsFolder = sourceFile.getParent().getParent().findChild("test-files");
            VirtualFile outputFolder = RecipeUtil.createTempFolder(module);

            ContainerDefinition containerDefinition = new ContainerDefinition();
            containerDefinition.setImageName(Constants.GPC_DEBUG_IMAGE);
            containerDefinition.setEnvironment(buildEnvironment(module.getProject(),"SECRET_"));
            containerDefinition.setMounts(buildMounts(sourceFile, configJsonFile, testsFolder, outputFolder));

            ScriptResultsView resultsView = new ScriptResultsView(module.getProject(),sourceFile.getParent(),outputFolder);
            List<VirtualFile> sourceFolders = getSourceFolders(module, sourceFile);

            Project project = module.getProject();

            ContainerProcessHandler handler = new ContainerProcessHandler(project, ()->
                    SwingUtilities.invokeLater(resultsView::refresh)
            );

            XDebugProcessStarter starter = new DebugProcessStarter(project, sourceFolders,Constants.DOCKER_SHARE_FOLDER,handler);
            XDebugSession session = XDebuggerManager.getInstance(project).startSessionAndShowTab("Debug script",null,starter);
            session.getConsoleView().attachToProcess(handler);

            runContainer(project, containerDefinition, handler::setContainer);

        }catch (Exception ex){
            Messages.showMessageDialog(module.getProject(), String.valueOf(ex.getMessage()), "Error", Messages.getErrorIcon());
        }
    }

    @NotNull
    private List<VirtualFile> getSourceFolders(Module module, VirtualFile scriptFile) {
        List<VirtualFile> sourceFolders = new ArrayList<>();
        sourceFolders.add(scriptFile.getParent()); // TODO: locate correct docker share

        VirtualFile resourcesFolder = RecipeUtil.recipeFolder(module).findChild("resources");
        if (resourcesFolder != null){
            sourceFolders.add(resourcesFolder);
        }
        return sourceFolders;
    }

    protected void runContainer(Project project, ContainerDefinition containerDefinition, Consumer<Container> consumer){

        Notification msg = UIUtil.showNotificationSync(project,"Launching container","");

        ApplicationManager.getApplication().invokeLater(()->{
            try {
                ContainerService.getInstance(project).createContainerAsync(containerDefinition,(Container c)->{
                    msg.hideBalloon();
                    consumer.accept(c);
                });
            }catch(ContainerServiceException ex){
                Messages.showErrorDialog(ex.getMessage(),"Error");
            }
        });
    }
}
