package io.datakitchen.ide.editors.neweditors.container;

import com.intellij.openapi.ui.DialogWrapper;
import io.datakitchen.ide.ui.FormPanel;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class SqlSinkKeyEditor extends DialogWrapper {
    private final JTextField fileName = new JTextField();
    private final JTextField tableName = new JTextField();

    public SqlSinkKeyEditor() {
        this(null);
    }

    public SqlSinkKeyEditor(String fileName) {
        super(true);
        if (fileName != null){
            this.fileName.setEnabled(false);
            this.fileName.setText(fileName);
        }
        init();
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        FormPanel panel = new FormPanel(new Dimension(300,28));
        JPanel containerFilePanel = new JPanel(new BorderLayout());
        containerFilePanel.add(new JLabel("docker-share/"), BorderLayout.WEST);
        containerFilePanel.add(fileName, BorderLayout.CENTER);
        panel.addField("Container file", containerFilePanel);
        panel.addField("Table name", tableName);
        return panel;
    }

    public String getFileName(){
        return fileName.getText();
    }

    public String getTableName(){
        return tableName.getText();
    }

//    public DataSinkSqlKey createKey(){
//        return new DataSinkSqlKey(connection, fileName.getText(), tableName.getText());
//    }

}
