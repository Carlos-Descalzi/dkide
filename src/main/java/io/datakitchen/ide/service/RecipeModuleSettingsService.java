package io.datakitchen.ide.service;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.module.Module;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;

@State(name="recipeModuleSettings")
public class RecipeModuleSettingsService implements PersistentStateComponent<RecipeModuleSettingsService.State> {


    public static class State {
        public final Map<String, String> properties = new LinkedHashMap<>();
    }

    @Override
    public @Nullable RecipeModuleSettingsService.State getState() {
        return state;
    }

    @Override
    public void loadState(@NotNull State state) {
        this.state = state;
    }

    private final Module module;
    private State state;

    public static RecipeModuleSettingsService getInstance(Module module){
        return module.getService(RecipeModuleSettingsService.class);
    }

    public RecipeModuleSettingsService(Module module){
        this.module = module;
    }

    private State state(){
        if (state == null){
            state = new State();
        }
        return state;
    }

    public void setProperty(String key, String value){
        state().properties.put(key, value);
    }

    public String getProperty(String key){
        return state().properties.get(key);
    }
}
