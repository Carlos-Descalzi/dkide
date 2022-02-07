package io.datakitchen.ide.editors.neweditors.container;

import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import io.datakitchen.ide.editors.CsvOptionsEditor;
import io.datakitchen.ide.model.CsvOptions;
import io.datakitchen.ide.model.DumpType;
import io.datakitchen.ide.ui.FormPanel;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class SQLSInkKeyDetailsDialog extends DialogWrapper {


    private final JTextField containerFileName = new JTextField();
    private final JTextField tableName = new JTextField();
    private final JLabel extension = new JLabel();
    private final CsvOptionsEditor csvOptionsEditor = new CsvOptionsEditor();

    protected SQLSInkKeyDetailsDialog(DataSinkSqlKey key) {
        super(true);
        init();
        loadKey(key);
    }

    private void loadKey(DataSinkSqlKey key) {

        String fileName = key.getContainerFileName();
        if (fileName.contains(".")){
            fileName = fileName.substring(0, fileName.indexOf('.'));
        }
        this.containerFileName.setText(fileName);
        this.tableName.setText(key.getTableName());
        extension.setText("."+DumpType.CSV.getExtension());
        if (key.getCsvOptions() != null){
            csvOptionsEditor.readOptions(key.getCsvOptions());
        }
        updateActions();
    }

    private void updateActions() {
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel fileNamePanel = new JPanel(new BorderLayout());
        fileNamePanel.add(containerFileName, BorderLayout.CENTER);
        fileNamePanel.add(extension, BorderLayout.EAST);
        FormPanel basicInfo = new FormPanel();
        basicInfo.addField("File name", fileNamePanel);
        basicInfo.addField("Table name", tableName);
        panel.add(basicInfo, BorderLayout.NORTH);
        panel.add(csvOptionsEditor, BorderLayout.CENTER);

        return panel;
    }

    @Override
    protected @NotNull List<ValidationInfo> doValidateAll() {
        List<ValidationInfo> validations = new ArrayList<>();

        if (StringUtils.isBlank(containerFileName.getText())){
            validations.add(new ValidationInfo("File name is required", containerFileName));
        }

        if (StringUtils.isBlank(tableName.getText())){
            validations.add(new ValidationInfo("Table name is required", tableName));
        }

        return validations;
    }

    public void writeToKey(DataSinkSqlKey key){
        key.setContainerFileName(containerFileName.getText()+"."+DumpType.CSV.getExtension());
        key.setTableName(tableName.getText());
        CsvOptions options = new CsvOptions();
        csvOptionsEditor.writeOptions(options);
        key.setCsvOptions(options);
    }
}
