package io.datakitchen.ide.dialogs;

import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import io.datakitchen.ide.ui.FormPanel;
import io.datakitchen.ide.ui.RegExValidatedField;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class EnterNameDialog extends DialogWrapper {

    private final RegExValidatedField name = new RegExValidatedField(RegExValidatedField.IDENTIFIER);

    public EnterNameDialog(){
        this(null);
    }

    public EnterNameDialog(String selectedNodeType){
        super(true);
        init();
    }
    @Override
    protected @NotNull List<ValidationInfo> doValidateAll() {
        List<ValidationInfo> validations = new ArrayList<>();

        if (StringUtils.isBlank(name.getText())){
            validations.add(new ValidationInfo("Name is required", name));
        }

        return validations;
    }

    public String getName(){
        return name.getText();
    }

    protected JComponent createCenterPanel(){
        FormPanel panel = new FormPanel();
        panel.addField("New node name",name,new Dimension(300,28));
        return panel;
    }

}
