package io.datakitchen.ide.dialogs;

import com.intellij.ide.projectView.ProjectView;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.util.ui.JBUI;
import io.datakitchen.ide.ui.FormLayout;
import io.datakitchen.ide.util.RecipeUtil;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class SetActiveVariationDialog extends DialogWrapper {

    private final Project project;
    private final Module module;
    private final JPanel panel = new JPanel();
    private final ComboBox<String> variationName = new ComboBox<>();

    public SetActiveVariationDialog(Project project, Module module){
        super(true);
        setTitle("Set active variation for recipe "+module.getName());
        this.project = project;
        this.module = module;

        panel.setLayout(new FormLayout(5,5));
        panel.setBorder(JBUI.Borders.empty(10));
        JLabel l = new JLabel("Variation");
        l.setLabelFor(variationName);
        panel.add(l);
        panel.add(variationName);
        variationName.setModel(new DefaultComboBoxModel<>(RecipeUtil.getVariations(module).toArray(String[]::new)));
        variationName.setPreferredSize(new Dimension(200,28));
        load();
        init();
    }


    @Override
    protected @Nullable JComponent createCenterPanel() {
        return panel;
    }

    public void save(){
        RecipeUtil.setActiveVariation(module, variationName.getItem());
        ProjectView.getInstance(project).refresh();
    }

    private void load(){
        variationName.setSelectedItem(RecipeUtil.getActiveVariation(module));
    }

}
