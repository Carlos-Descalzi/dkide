package io.datakitchen.ide.editors.neweditors.mapper;

import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.DialogWrapper;
import io.datakitchen.ide.editors.CsvOptionsEditor;
import io.datakitchen.ide.model.CsvOptions;
import io.datakitchen.ide.model.DumpType;
import io.datakitchen.ide.ui.FormPanel;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class SqlSinkKeyDetailsDialog extends DialogWrapper {


    private final JTextField tableName = new JTextField();
    private final ComboBox<DumpType> dumpType = new ComboBox<>(new DumpType[]{DumpType.CSV, DumpType.BINARY, DumpType.JSON});
    private final CsvOptionsEditor csvOptionsEditor = new CsvOptionsEditor();

    protected SqlSinkKeyDetailsDialog(DataSinkSqlKey key) {
        super(true);
        init();
        dumpType.addActionListener(e -> updateActions());
        loadKey(key);
    }

    private void loadKey(DataSinkSqlKey key) {
        tableName.setText(key.getTableName());
        dumpType.setSelectedItem(key.getDumpType());
        dumpType.setRenderer(new DefaultListCellRenderer(){
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                return super.getListCellRendererComponent(list, ((DumpType)value).getDescription(), index, isSelected, cellHasFocus);
            }
        });
        if (key.getCsvOptions() != null){
            csvOptionsEditor.readOptions(key.getCsvOptions());
        }
        updateActions();
    }

    private void updateActions() {
        csvOptionsEditor.enableForType(dumpType.getItem());
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        FormPanel basicInfo = new FormPanel();
        basicInfo.addField("Table name", tableName);
        basicInfo.addField("Input format", dumpType);
        panel.add(basicInfo, BorderLayout.NORTH);
        panel.add(csvOptionsEditor, BorderLayout.CENTER);

        return panel;
    }

    public void writeToKey(DataSinkSqlKey key){
        key.setTableName(tableName.getText());
        key.setDumpType(dumpType.getItem());
        CsvOptions options = new CsvOptions();
        csvOptionsEditor.writeOptions(options);
        key.setCsvOptions(options);
    }
}
