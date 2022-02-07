package io.datakitchen.ide.config.editors;

import com.intellij.openapi.ui.ComboBox;
import io.datakitchen.ide.HelpMessages;
import io.datakitchen.ide.config.ProjectSettings;
import io.datakitchen.ide.module.Site;
import io.datakitchen.ide.ui.FormPanel;
import io.datakitchen.ide.ui.HelpContainer;

import javax.swing.*;
import java.awt.*;

public class ProjectSettingsEditor extends FormPanel {

    private final JTextField kitchenName = new JTextField();
    private final ComboBox<Site> ingredientSite = new ComboBox<>(new DefaultComboBoxModel<>(Site.DEFAULT_SITES));

    public ProjectSettingsEditor(){
        super(new Dimension(200,28),new Dimension(200,28));

        addField("Kitchen Name",new HelpContainer(kitchenName, HelpMessages.CONFIG_KITCHEN_NAME_MSG));
        addField("Ingredients site" ,new HelpContainer(ingredientSite, HelpMessages.CONFIG_INGREDIENT_SITE));

    }

    public void setProjectSettings(ProjectSettings settings){
        kitchenName.setText(settings.getKitchenName());
        ingredientSite.setSelectedItem(Site.siteByUrl(settings.getIngredientSite()));
    }

    public ProjectSettings getProjectSettings(){
        ProjectSettings settings = new ProjectSettings();
        settings.setKitchenName(kitchenName.getText());
        Site site = (Site)ingredientSite.getSelectedItem();
        settings.setIngredientSite(site == null ? null : site.getUrl());
        return settings;
    }
}
