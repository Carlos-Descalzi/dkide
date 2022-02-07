package io.datakitchen.ide.editors.neweditors.ingredient;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.vfs.VirtualFile;
import io.datakitchen.ide.editors.neweditors.AbstractNodeModel;
import io.datakitchen.ide.model.*;

import java.util.LinkedHashMap;
import java.util.Map;

public class IngredientNodeModelImpl extends AbstractNodeModel implements IngredientNodeModel {

    private String ingredientKitchen;
    private String ingredientRecipe;
    private String ingredientName;
    private Integer pollInterval;
    private Integer timeout;
    private Map<String, Object> inputParameters = new LinkedHashMap<>();

    public IngredientNodeModelImpl(Module module, VirtualFile nodeFolder) {
        super(module, nodeFolder);
    }

    @Override
    public String getIngredientKitchen() {
        return ingredientKitchen;
    }

    @Override
    public void setIngredientKitchen(String ingredientKitchen) {
        String oldValue = this.ingredientKitchen;
        this.ingredientKitchen = ingredientKitchen;
        notifyPropertyChanged("ingredientKitchen",oldValue, ingredientKitchen);
    }

    @Override
    public String getIngredientRecipe() {
        return ingredientRecipe;
    }

    @Override
    public void setIngredientRecipe(String ingredientRecipe) {
        String oldValue = this.ingredientRecipe;
        this.ingredientRecipe = ingredientRecipe;
        notifyPropertyChanged("ingredientRecipe",oldValue, ingredientRecipe);
    }

    @Override
    public String getIngredientName() {
        return ingredientName;
    }

    @Override
    public void setIngredientName(String ingredientName) {
        String oldValue = this.ingredientName;
        this.ingredientName = ingredientName;
        notifyPropertyChanged("ingredientName",oldValue, ingredientName);
    }

    @Override
    public Integer getPollInterval() {
        return pollInterval;
    }

    @Override
    public void setPollInterval(Integer pollInterval) {
        Integer oldValue = this.pollInterval;
        this.pollInterval = pollInterval;
        notifyPropertyChanged("pollInterval",oldValue, pollInterval);
    }

    @Override
    public Integer getTimeout() {
        return timeout;
    }

    @Override
    public void setTimeout(Integer timeout) {
        Integer oldValue = this.timeout;
        this.timeout = timeout;
        notifyPropertyChanged("timeout",oldValue, timeout);
    }

    @Override
    public Map<String, Object> getInputParameters() {
        return inputParameters;
    }

    @Override
    public void setInputParameters(Map<String, Object> inputParameters) {
        Map<String, Object> oldValue = this.inputParameters;
        this.inputParameters = inputParameters;
        notifyPropertyChanged("inputParameters",oldValue, inputParameters);
    }

}
