package io.datakitchen.ide.editors.neweditors.container;

import com.intellij.openapi.ui.ComboBox;
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

public class SQLSourceKeyDetailsDialog extends DialogWrapper {


    private final JTextField fileName = new JTextField();
    private final JLabel extension = new JLabel();
    private final ComboBox<DumpType> dumpType = new ComboBox<>(new DumpType[]{DumpType.CSV, DumpType.BINARY, DumpType.JSON});
    private final CsvOptionsEditor csvOptionsEditor = new CsvOptionsEditor();

    protected SQLSourceKeyDetailsDialog(DataSourceSqlKey key) {
        super(true);
        init();
        dumpType.addActionListener(e -> updateActions());
        loadKey(key);
    }

    private void loadKey(DataSourceSqlKey key) {

        String fileName = key.getContainerFileName();
        if (fileName.contains(".")){
            fileName = fileName.substring(0, fileName.indexOf('.'));
        }
        this.fileName.setText(fileName);
        extension.setText("."+key.getDumpType().getExtension());
        dumpType.setSelectedItem(key.getDumpType());
        if (key.getCsvOptions() != null){
            csvOptionsEditor.readOptions(key.getCsvOptions());
        }
        updateActions();
    }

    private void updateActions() {
//        csvOptionsEditor.setEnabled(dumpType.getItem() == DumpType.CSV);
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel fileNamePanel = new JPanel(new BorderLayout());
        fileNamePanel.add(fileName, BorderLayout.CENTER);
        fileNamePanel.add(extension, BorderLayout.EAST);
        dumpType.addActionListener(e -> extension.setText("."+dumpType.getItem().getExtension()));
        FormPanel basicInfo = new FormPanel();
        basicInfo.addField("File name", fileNamePanel);
        basicInfo.addField("Input format", dumpType);
        panel.add(basicInfo, BorderLayout.NORTH);
        panel.add(csvOptionsEditor, BorderLayout.CENTER);

        return panel;
    }

    @Override
    protected @NotNull java.util.List<ValidationInfo> doValidateAll() {
        List<ValidationInfo> validations = new ArrayList<>();

        if (StringUtils.isBlank(fileName.getText())){
            validations.add(new ValidationInfo("File name is required", fileName));
        }

        return validations;
    }

    public void writeToKey(DataSourceSqlKey key){
        key.setContainerFileName(fileName.getText()+"."+dumpType.getItem().getExtension());
        key.setDumpType(dumpType.getItem());
        CsvOptions options = new CsvOptions();
        csvOptionsEditor.writeOptions(options);
        key.setCsvOptions(options);
    }
}
