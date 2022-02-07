package io.datakitchen.ide.run;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.content.impl.ContentImpl;
import io.datakitchen.ide.Constants;
import io.datakitchen.ide.config.Secret;
import io.datakitchen.ide.service.CompilerService;
import io.datakitchen.ide.service.ContainerDefinition;
import io.datakitchen.ide.tools.ScriptResultsView;
import io.datakitchen.ide.util.JsonUtil;
import io.datakitchen.ide.util.RecipeUtil;
import net.minidev.json.parser.ParseException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.io.*;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class ScriptRunner extends Runner{

    private static final Logger LOGGER = Logger.getInstance(ScriptRunner.class);

    protected final VirtualFile sourceFile;

    public ScriptRunner(Module module, VirtualFile sourceFile) {
        super(module);
        this.sourceFile = sourceFile;
    }

    @Override
    public void run() {

        VirtualFile configJsonFile = sourceFile.getParent().findChild("config.json");

        CompilerService.getInstance(module).compileFile(configJsonFile, (String configJson)->{

            try {
                configJson = modifyConfigJson(configJson, sourceFile);
                File tempConfigJsonFile = File.createTempFile("config", ".json");
                FileUtils.write(tempConfigJsonFile, configJson, "utf-8");

                doRunScript(module, sourceFile, tempConfigJsonFile);
            }catch (Exception ex){
                Messages.showMessageDialog(module.getProject(), String.valueOf(ex.getMessage()), "Error", Messages.getErrorIcon());
            }

        });
    }

    @SuppressWarnings("unchecked")
    private String modifyConfigJson(String configJson, VirtualFile scriptFile) throws IOException, ParseException {
        Map<String, Object> configJsonObj = JsonUtil.read(configJson);

        Map<String, Object> keys = (Map<String, Object>)configJsonObj.get("keys");
        Map<String, Object> newKeys = new LinkedHashMap<>();

        for (Map.Entry<String, Object> keyEntry : keys.entrySet()){
            String keyName = keyEntry.getKey();

            Map<String, Object> content = (Map<String, Object>) keyEntry.getValue();

            if (content.get("script").equals(scriptFile.getName())){
                newKeys.put(keyName, content);
            }
        }

        configJsonObj.put("keys",newKeys);

        return JsonUtil.toJsonString(configJsonObj);
    }

    protected void doRunScript(Module module, VirtualFile scriptFile, File configJsonFile){
        try {
            VirtualFile testsFolder = scriptFile.getParent().getParent().findChild("test-files");
            VirtualFile outputFolder = RecipeUtil.createTempFolder(module);

            ContainerDefinition containerDefinition = new ContainerDefinition();
            containerDefinition.setImageName(Constants.GPC_IMAGE);
            containerDefinition.setEnvironment(buildEnvironment(module.getProject(),"SECRET_"));
            containerDefinition.setMounts(buildMounts(scriptFile, configJsonFile, testsFolder, outputFolder));

            ScriptResultsView resultsView = new ScriptResultsView(module.getProject(),scriptFile.getParent(),outputFolder);

            runContainer("Run script "+scriptFile.getName(),module.getProject(), containerDefinition, ()->
                        SwingUtilities.invokeLater(()->
                            outputFolder.refresh(true, true, resultsView::refresh)
                        )
                    , getDiskUsageSupplier(outputFolder),
                    new ContentImpl(resultsView,"Output",false));
        }catch (Exception ex){
            Messages.showMessageDialog(module.getProject(), String.valueOf(ex.getMessage()), "Error", Messages.getErrorIcon());
        }
    }

    @NotNull
    protected Map<String, String> buildMounts(VirtualFile scriptFile, File configJsonFile, VirtualFile testsFolder, VirtualFile outputFolder) {
        Map<String, String> mounts = new HashMap<>();
        mounts.put(outputFolder.getPath(), Constants.DOCKER_SHARE_FOLDER);

        // Copy the resources to the mounted volume.
        // If I just mount the files, at the end will leave empty files on the place
        // of those mounted files, and the file view may not be able to display them.
        ApplicationManager.getApplication().runWriteAction(()->{
            try {
                for (VirtualFile scriptItem : scriptFile.getParent().getChildren()) {
                    if (scriptItem.getName().equals("config.json")) {
                        VirtualFile vFile = outputFolder.createChildData(this, "config.json");

                        try (InputStream in = new FileInputStream(configJsonFile); OutputStream out = vFile.getOutputStream(this)){
                            IOUtils.copy(in, out);
                        }
                    } else {
                        VfsUtil.copy(this, scriptItem, outputFolder);
                    }
                }

                if (testsFolder != null) {
                    for (VirtualFile testFile : testsFolder.getChildren()) {
                        VfsUtil.copy(this, testFile, outputFolder);
                    }
                }
            }catch (IOException ex){
                LOGGER.error(ex);
            }
        });
        return mounts;
    }
    protected Map<String, String> buildEnvironment(Project project) {
        return buildEnvironment(project, null);
    }

    protected Map<String, String> buildEnvironment(Project project, String secretPrefix) {
        Map<String, String> environment = new HashMap<>();
        environment.put("INSIDE_CONTAINER_FILE_MOUNT",Constants.GPC_WORK_DIR);
        environment.put("INSIDE_CONTAINER_FILE_DIRECTORY",Constants.DOCKER_SHARE_FOLDER_NAME);
        environment.put("CONTAINER_INPUT_CONFIG_FILE_PATH",Constants.DOCKER_SHARE_FOLDER + "/config.json");
        environment.put("CONTAINER_INPUT_CONFIG_FILE_NAME","config.json");
        environment.put("CONTAINER_OUTPUT_PROGRESS_FILE","ac_progress.json");
        environment.put("CONTAINER_OUTPUT_LOG_FILE","ac_logger.log");

        List<Secret> secrets = getSecrets(project);
        if (secrets != null) {
            for (Secret secret : secrets) {
                String envKey = (secretPrefix != null ? secretPrefix : "")
                        + secret.getPath().replace("/", "_")
                            .replace("-","_")
                            .toUpperCase();
                environment.put(envKey, secret.getValue());
            }
        }

        return environment;
    }

    protected Supplier<Long> getDiskUsageSupplier(VirtualFile path){
        return () -> FileUtils.sizeOfDirectory(new File(path.getPath()));

    }

}
