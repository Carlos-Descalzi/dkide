package io.datakitchen.ide.dialogs;

import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.TextBrowseFolderListener;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.ui.ValidationInfo;
import io.datakitchen.ide.HelpMessages;
import io.datakitchen.ide.ui.FormPanel;
import io.datakitchen.ide.ui.HelpContainer;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class CLINewRecipeDialog extends DialogWrapper {

    private final TextFieldWithBrowseButton kitchenFolder = new TextFieldWithBrowseButton();
    private final JTextField recipeName = new JTextField();

    public CLINewRecipeDialog() {
        super(true);
        setTitle("New Recipe");
        FileChooserDescriptor descriptor = new FileChooserDescriptor(
            false,
            true,
            false,
            false,
            false,
            false
        );
        kitchenFolder.addBrowseFolderListener(new TextBrowseFolderListener(descriptor));
        init();
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        FormPanel panel  =new FormPanel();
        panel.addField("Kitchen path",new HelpContainer(kitchenFolder, HelpMessages.KITCHEN_PATH_MESSAGE));
        panel.addField("Recipe name", new HelpContainer(recipeName,HelpMessages.RECIPE_NAME_MESSAGE));
        return panel;
    }

    @Override
    protected @NotNull List<ValidationInfo> doValidateAll() {
        List<ValidationInfo> validations = new ArrayList<>();

        if (StringUtils.isBlank(kitchenFolder.getText())){
            validations.add(new ValidationInfo("Kitchen path is required", kitchenFolder));
        }

        if (StringUtils.isBlank(recipeName.getText())){
            validations.add(new ValidationInfo("Recipe name is required", recipeName));
        }

        return validations;
    }

    public String getKitchenPath(){
        return kitchenFolder.getText();
    }

    public String getRecipeName(){
        return recipeName.getText();
    }
}
