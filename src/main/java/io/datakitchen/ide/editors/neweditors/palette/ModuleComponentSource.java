package io.datakitchen.ide.editors.neweditors.palette;

import com.intellij.openapi.module.Module;
import io.datakitchen.ide.util.RecipeUtil;

import java.util.Map;

public class ModuleComponentSource implements ComponentSource{
    private final Module module;

    public ModuleComponentSource(Module module){
        this.module = module;
    }

    @Override
    public Map<String, Object> getAllVariables() {
        return RecipeUtil.loadAllVariables(module);
    }
}
