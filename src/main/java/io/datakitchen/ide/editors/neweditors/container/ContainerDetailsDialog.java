package io.datakitchen.ide.editors.neweditors.container;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import io.datakitchen.ide.ui.EntryField;
import io.datakitchen.ide.ui.FormPanel;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class ContainerDetailsDialog extends DialogWrapper {

    private Module module;
    private EntryField image;
    private EntryField username;
    private EntryField password;
    private EntryField registryUrl;


    protected ContainerDetailsDialog(Module module) {
        super(true);
        setTitle("Edit Container Details");
        this.module = module;
        init();
    }

    public void setDetails(String imageName, String username, String password, String registryUrl){
        this.image.setText(imageName);
        this.username.setText(username);
        this.password.setText(password);
        this.registryUrl.setText(registryUrl);
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        FormPanel panel = new FormPanel();
        image = new EntryField(module);
        username = new EntryField(module);
        password = new EntryField(module);
        registryUrl = new EntryField(module);
        panel.addField("Image name", image);
        panel.addField("Registry URL", registryUrl);
        panel.addField("Username", username);
        panel.addField("Password", password);
        return panel;
    }

    @Override
    protected @NotNull List<ValidationInfo> doValidateAll() {
        List<ValidationInfo> validations = new ArrayList<>();

        if (StringUtils.isBlank(image.getText())){
            validations.add(new ValidationInfo("Image name is required", image));
        }

        return validations;
    }

    public String getImageName() {
        return this.image.getText();
    }

    public String getUsername() {
        return this.username.getText();
    }

    public String getPassword() {
        return this.password.getText();
    }

    public String getRegistry() {
        return this.registryUrl.getText();
    }
}
