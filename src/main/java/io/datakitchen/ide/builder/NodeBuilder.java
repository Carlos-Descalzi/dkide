package io.datakitchen.ide.builder;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import io.datakitchen.ide.Constants;
import io.datakitchen.ide.config.ConfigurationService;
import io.datakitchen.ide.util.JsonUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class NodeBuilder {
    private final Project project;
    private Module module;
    private String nodeName;
    private String nodeType;

    public NodeBuilder(Project project){
        this.project = project;
    }

    public NodeBuilder setModule(Module module) {
        this.module = module;
        return this;
    }

    public NodeBuilder setNodeName(String nodeName) {
        this.nodeName = nodeName;
        return this;
    }

    public NodeBuilder setNodeType(String nodeType) {
        this.nodeType = nodeType;
        return this;
    }

    public void build(Consumer<VirtualFile> onFinish){
        ApplicationManager.getApplication().runWriteAction(()->{
            try {
                doBuild(onFinish);
            }catch (Exception ex){
                ex.printStackTrace();
            }
        });
    }

    private void doBuild(Consumer<VirtualFile> onFinish) throws IOException {
        VirtualFile recipeFolder = module.getModuleFile().getParent();

        VirtualFile nodeFolder = recipeFolder.createChildDirectory(this, this.nodeName);

        List<String> lines;
        try (InputStream input = getClass().getResourceAsStream("/templates/nodes/"+nodeType+"/files.txt")){
            lines = IOUtils.readLines(new InputStreamReader(input));
        }
        for (String fileName:lines){
            String sourcePath = "/templates/nodes/"+nodeType+ File.separator+ fileName;
            String targetPath = nodeName + File.separator+ fileName;
            if (targetPath.endsWith("/")){
                new File(recipeFolder.getPath(), targetPath).mkdirs();
            } else {
                try (InputStream input = getClass().getResourceAsStream(sourcePath)) {
                    File targetFile = new File(recipeFolder.getPath(), targetPath);
                    targetFile.getParentFile().mkdirs();
                    FileUtils.copyToFile(input, targetFile);
                }
            }
        }

        recipeFolder.refresh(true, true, () -> {
            ApplicationManager.getApplication().runWriteAction(()->{
                try {
                    setupNode(nodeFolder);
                }catch(Exception ex){}
            });
            if (onFinish != null){
                onFinish.accept(nodeFolder);
            }
        });
    }

    private void setupNode(VirtualFile nodeFolder) throws Exception{
        VirtualFile descriptionFile = nodeFolder.findChild(Constants.FILE_DESCRIPTION_JSON);

        Map<String, Object> descriptionJson = JsonUtil.read(descriptionFile);

        boolean simplifiedView = ConfigurationService.getInstance(project)
                .getGlobalConfiguration()
                .getMiscOptions()
                .isSimplifiedView();

        if (simplifiedView){
            Map<String, Object> options = new LinkedHashMap<>();
            options.put("simplified-view", true);
            descriptionJson.put("options", options);

            JsonUtil.write(descriptionJson, descriptionFile);
        }
    }
}
