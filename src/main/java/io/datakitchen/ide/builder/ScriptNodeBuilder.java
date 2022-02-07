package io.datakitchen.ide.builder;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import io.datakitchen.ide.model.NodeType;
import io.datakitchen.ide.util.JsonUtil;
import io.datakitchen.ide.util.ObjectUtil;
import io.datakitchen.ide.util.RecipeUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.function.Consumer;

public class ScriptNodeBuilder {
    private final Module module;
    private File scriptFile;

    public ScriptNodeBuilder(Module module){
        this.module = module;
    }

    public ScriptNodeBuilder setScriptFile(File scriptFile){
        this.scriptFile = scriptFile;
        return this;
    }

    public void build(Consumer<VirtualFile> onFinish){

        String nodeName = "Run_"+scriptFile.getName()
                .replace(".","_")
                .replace("-","_");

        VirtualFile recipeFolder = RecipeUtil.recipeFolder(module);

        nodeName = adjustNodeName(recipeFolder, nodeName);

        new NodeBuilder(module.getProject())
            .setNodeName(nodeName)
                .setModule(module)
                .setNodeType(NodeType.SCRIPT_NODE.getTypeName())
                .build((VirtualFile nodeFolder)->{
                    removeDefaultScript(nodeFolder);
                    addFile(nodeFolder, onFinish);
                });
    }

    private void removeDefaultScript(VirtualFile nodeFolder) {
        try {
            VirtualFile scriptsFolder = nodeFolder.findChild("docker-share");
            VirtualFile defaultScript = scriptsFolder.findChild("main.py");
            if (defaultScript != null) {
                defaultScript.delete(this);
            }
        }catch(IOException ex){
            ex.printStackTrace();
        }
    }

    private String adjustNodeName(VirtualFile recipeFolder, String nodeName) {
        if (recipeFolder.findChild(nodeName) == null){
            return nodeName;
        }
        int suffix = 1;

        while (recipeFolder.findChild(nodeName+"_"+suffix) != null){
            suffix++;
        }
        return nodeName+"_"+suffix;
    }

    private void addFile(VirtualFile nodeFolder, Consumer<VirtualFile> onFinish) {
        try {
            VirtualFile scriptsFolder = nodeFolder.findChild("docker-share");
            VirtualFile configJsonFile = scriptsFolder.findChild("config.json");

            VirtualFile vFile = VirtualFileManager.getInstance().findFileByNioPath(Path.of(scriptFile.toURI()));
            VfsUtil.copyFile(this, vFile, scriptsFolder);

            modifyConfigJson(configJsonFile);

            onFinish.accept(nodeFolder);
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }

    private void modifyConfigJson(VirtualFile configJsonFile) throws Exception{
        Map<String, Object> configJson = JsonUtil.read(configJsonFile);
        Map<String, Object> keys = ObjectUtil.cast(configJson.get("keys"));
        Map<String, Object> runKey = ObjectUtil.cast(keys.get("run"));
        runKey.put("script",scriptFile.getName());

        JsonUtil.write(configJson, configJsonFile);
    }
}
