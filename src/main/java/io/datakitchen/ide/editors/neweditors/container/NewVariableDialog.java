package io.datakitchen.ide.editors.neweditors.container;

import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import io.datakitchen.ide.model.Assignment;
import io.datakitchen.ide.ui.FormPanel;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class NewVariableDialog extends DialogWrapper {
    private JTextField fileName;
    private JTextField variableName;

    public NewVariableDialog() {
        super(true);
        init();
    }


    @Override
    protected @Nullable JComponent createCenterPanel() {
        FormPanel panel = new FormPanel(new Dimension(300,28));
        fileName = new JTextField();
        variableName = new JTextField();

        JPanel fileNamePanel = new JPanel(new BorderLayout());
        fileNamePanel.add(new JLabel("docker-share/"), BorderLayout.WEST);
        fileNamePanel.add(fileName, BorderLayout.CENTER);

        panel.addField("Container file", fileNamePanel);
        panel.addField("Variable name", variableName);

        return panel;
    }

    @Override
    protected @NotNull java.util.List<ValidationInfo> doValidateAll() {
        List<ValidationInfo> validations = new ArrayList<>();

        if (StringUtils.isBlank(variableName.getText())){
            validations.add(new ValidationInfo("Variable name is required", variableName));
        }

        if (StringUtils.isBlank(fileName.getText())){
            validations.add(new ValidationInfo("Container file is required", fileName));
        }

        return validations;
    }

    public Assignment getAssignment(){
        return new Assignment(fileName.getText(), variableName.getText());
    }
}
