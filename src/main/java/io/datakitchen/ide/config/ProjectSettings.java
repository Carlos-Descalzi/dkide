package io.datakitchen.ide.config;

import java.io.Serializable;

public class ProjectSettings implements Serializable {

    private static final long serialVersionUID = 1;

    private String kitchenName = null;
    private String ingredientSite = null;

    public String getKitchenName() {
        return kitchenName;
    }

    public void setKitchenName(String kitchenName) {
        this.kitchenName = kitchenName;
    }

    public String getIngredientSite() {
        return ingredientSite;
    }

    public void setIngredientSite(String ingredientSite) {
        this.ingredientSite = ingredientSite;
    }
}
