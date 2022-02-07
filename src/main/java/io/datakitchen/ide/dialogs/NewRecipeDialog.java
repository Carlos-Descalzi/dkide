package io.datakitchen.ide.dialogs;

import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import io.datakitchen.ide.ui.FormPanel;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class NewRecipeDialog extends DialogWrapper {
    private final JTextField recipeName = new JTextField();

    public NewRecipeDialog() {
        super(true);
        setTitle("New Recipe");
        init();
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        FormPanel panel = new FormPanel();
        panel.addField("Recipe Name", recipeName);
        return panel;
    }

    @Override
    protected @NotNull List<ValidationInfo> doValidateAll() {
        List<ValidationInfo> validations = new ArrayList<>();

        if (StringUtils.isBlank(recipeName.getText())){
            validations.add(new ValidationInfo("Recipe name is required", recipeName));
        }

        return validations;
    }

    public String getRecipeName(){
        return recipeName.getText();
    }
}
