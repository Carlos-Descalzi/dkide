package io.datakitchen.ide.model;

import java.util.Map;

public interface IngredientNodeModel extends NodeModel{

    String getIngredientKitchen();
    void setIngredientKitchen(String ingredientKitchen);
    String getIngredientRecipe();
    void setIngredientRecipe(String ingredientRecipe);
    String getIngredientName();
    void setIngredientName(String ingredientName);
    Integer getPollInterval();
    void setPollInterval(Integer pollInterval);
    Integer getTimeout();
    void setTimeout(Integer timeout);
    Map<String, Object> getInputParameters();
    void setInputParameters(Map<String, Object> inputParameters);
}
