package io.datakitchen.ide.editors.neweditors.container;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import io.datakitchen.ide.model.ScriptNodeKey;
import io.datakitchen.ide.util.JsonUtil;
import net.minidev.json.parser.ParseException;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class ScriptModelWriter extends ContainerModelWriter{
    public ScriptModelWriter(Project project, ContainerModelImpl model) {
        super(project, model);
    }

    @Override
    protected void doWrite(VirtualFile nodeFolder) throws IOException, ParseException {
        super.doWrite(nodeFolder);
        writeConfigJson(nodeFolder);
    }

    private void writeConfigJson(VirtualFile nodeFolder) throws IOException {
        ScriptNodeModelImpl model = (ScriptNodeModelImpl) this.model;

        Map<String, Object> configJson = new LinkedHashMap<>();

        configJson.put("apt-dependencies",model.getAptDependencies());
        configJson.put("dependencies",model.getPipDependencies());

        Map<String, Object> keysJson = new LinkedHashMap<>();
        configJson.put("keys", keysJson);
        int i=1;
        for (ScriptNodeKey key: model.getKeys()){
            Map<String, Object> keyJson = new LinkedHashMap<>();

            keyJson.put("script", key.getScript());
            keyJson.put("parameters", key.getParameters());
            keyJson.put("environment",key.getEnvironment());
            keyJson.put("export", key.getExports());

            keysJson.put("key-"+(i++), keyJson);
        }

        VirtualFile dockerShareFolder = nodeFolder.findChild("docker-share");

        VirtualFile configJsonFile = dockerShareFolder.findChild("config.json");
        if (configJsonFile == null){
            configJsonFile = dockerShareFolder.createChildData(this,"config.json");
        }
        JsonUtil.write(configJson, configJsonFile);

    }
}
