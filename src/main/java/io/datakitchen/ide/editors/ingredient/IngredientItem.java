package io.datakitchen.ide.editors.ingredient;

import io.datakitchen.ide.ui.NamedObject;

import java.util.LinkedHashMap;
import java.util.Map;

public class IngredientItem implements NamedObject {
    private final Map<String, Object> ingredient;

    public IngredientItem() {
        this(new LinkedHashMap<>());
    }

    public IngredientItem(Map<String, Object> ingredient) {
        this.ingredient = ingredient;
    }

    public IngredientItem(String name) {
        this(new LinkedHashMap<>());
        ingredient.put("ingredient-name", name);
    }

    public void setName(String name){
        ingredient.put("ingredient-name",name);
    }

    public String getName(){
        return (String)ingredient.get("ingredient-name");
    }

    public String toString() {
        return (String) ingredient.getOrDefault("ingredient-name", "(no name)");
    }

    public Map<String, Object> getContent() {
        return ingredient;
    }
}
