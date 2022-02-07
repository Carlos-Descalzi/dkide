package io.datakitchen.ide.editors.neweditors.container;

import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import io.datakitchen.ide.ui.FormPanel;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class NewFileDialog extends DialogWrapper {
    private final JTextField fileName = new JTextField();

    protected NewFileDialog(boolean testFile) {
        super(true);
        setTitle(testFile ? "Create New Test File" : "Create New File");
        init();
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        FormPanel panel = new FormPanel();
        JPanel p = new JPanel(new BorderLayout());
        p.add(new JLabel("docker-share/"), BorderLayout.WEST);
        p.add(fileName);
        panel.addField("File name",p);
        return p;
    }

    @Override
    protected @NotNull java.util.List<ValidationInfo> doValidateAll() {
        List<ValidationInfo> validations = new ArrayList<>();

        if (StringUtils.isBlank(fileName.getText())){
            validations.add(new ValidationInfo("File name is required", fileName));
        }

        return validations;
    }

    public String getFileName(){
        return fileName.getText();
    }
}
