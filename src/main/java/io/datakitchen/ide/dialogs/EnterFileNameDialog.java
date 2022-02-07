package io.datakitchen.ide.dialogs;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.openapi.vfs.VirtualFile;
import io.datakitchen.ide.ui.FormPanel;
import io.datakitchen.ide.util.RecipeUtil;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class EnterFileNameDialog extends DialogWrapper {

    private final JTextField name = new JTextField();
    private final Module module;

    public EnterFileNameDialog(Module module){
        this(module, null);
    }

    public EnterFileNameDialog(Module module, String selectedNodeType){
        super(true);
        this.module = module;
        init();
    }
    @Override
    protected @NotNull List<ValidationInfo> doValidateAll() {
        List<ValidationInfo> validations = new ArrayList<>();

        if (StringUtils.isBlank(name.getText())){
            validations.add(new ValidationInfo("File Name is required", name));
        } else {
            VirtualFile resourcesFolder = RecipeUtil.recipeFolder(module).findChild("resources");
            if (resourcesFolder.findChild(name.getText()) != null){
                validations.add(new ValidationInfo("File Name already exists", name));
            }
        }


        return validations;
    }

    public String getName(){
        return name.getText();
    }

    protected JComponent createCenterPanel(){
        FormPanel panel = new FormPanel();
        panel.addField("New File Name",name,new Dimension(300,28));
        return panel;
    }

}
