package io.datakitchen.ide.dialogs;

import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.TextBrowseFolderListener;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.ui.ValidationInfo;
import io.datakitchen.ide.ui.FormPanel;
import io.datakitchen.ide.util.RecipeUtil;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class AddRecipeDialog extends DialogWrapper {

    private final FormPanel panel = new FormPanel();
    private final TextFieldWithBrowseButton folderChooser;

    public AddRecipeDialog() {
        super(true);
        setTitle("Add Recipe to Workspace");

        folderChooser = new TextFieldWithBrowseButton();
        FileChooserDescriptor descriptor = new FileChooserDescriptor(false, true,false,false,false,false);

        folderChooser.addBrowseFolderListener(new TextBrowseFolderListener(descriptor));

        panel.addField("Recipe path",folderChooser);
        init();
    }

    @Override
    protected @NotNull List<ValidationInfo> doValidateAll() {
        List<ValidationInfo> validations = new ArrayList<>();

        String path = folderChooser.getText();

        if (StringUtils.isBlank(path)){
            validations.add(new ValidationInfo("Recipe path is required", folderChooser));
        } else if (!RecipeUtil.isRecipeFolder(path)){
            validations.add(new ValidationInfo("Selected folder is not a recipe", folderChooser));
        }

        return validations;
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        return panel;
    }

    public String getPath(){
        return folderChooser.getText();
    }
}
