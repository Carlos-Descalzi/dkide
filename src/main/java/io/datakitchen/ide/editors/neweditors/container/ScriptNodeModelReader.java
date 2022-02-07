package io.datakitchen.ide.editors.neweditors.container;

import com.intellij.openapi.vfs.VirtualFile;
import io.datakitchen.ide.editors.neweditors.palette.ComponentSource;
import io.datakitchen.ide.json.CustomJsonParser;
import io.datakitchen.ide.model.ScriptNodeKey;
import io.datakitchen.ide.util.ObjectUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ScriptNodeModelReader extends ContainerModelReader{
    public ScriptNodeModelReader(VirtualFile nodeFolder) {
        super(nodeFolder);
    }

    @Override
    public void read(ContainerModelImpl model, ComponentSource componentSource) throws Exception {
        super.read(model, componentSource);
        readConfigJson((ScriptNodeModelImpl)model);
    }

    private void readConfigJson(ScriptNodeModelImpl model) throws IOException, io.datakitchen.ide.json.ParseException {
        VirtualFile configJsonFile = getNodeFolder().findChild("docker-share").findChild("config.json");
        Map<String, Object> configJson = CustomJsonParser.parse(configJsonFile);
        Map<String, Object> keys = ObjectUtil.cast( configJson.get("keys"));
        for (Object keyObj: keys.values()){
            Map<String, Object> key = ObjectUtil.cast(keyObj);

            ScriptNodeKey scriptNodeKey = new ScriptNodeKey();
            scriptNodeKey.setScript((String)key.get("script"));
            scriptNodeKey.setEnvironment( ObjectUtil.cast(key.get("environment")));
            scriptNodeKey.setParameters(ObjectUtil.cast(key.get("parameters")));
            scriptNodeKey.setExports(ObjectUtil.cast(key.get("export")));

            model.addKey(scriptNodeKey);
        }
        List<String> pipDependencies = ObjectUtil.cast(configJson.get("dependencies"));
        if (pipDependencies == null){
            pipDependencies = new ArrayList<>();
        }
        model.setPipDependencies(pipDependencies);
        List<String> aptDependencies = ObjectUtil.cast(configJson.get("apt-dependencies"));
        if (aptDependencies == null){
            aptDependencies = new ArrayList<>();
        }
        model.setAptDependencies(aptDependencies);
    }
}
