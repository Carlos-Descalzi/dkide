package io.datakitchen.ide.editors;

import io.datakitchen.ide.model.CsvOptions;
import io.datakitchen.ide.model.DumpType;
import io.datakitchen.ide.ui.FormPanel;

import javax.swing.*;

public class CsvOptionsEditor extends FormPanel {
    private final JTextField columnDelimiter = new JTextField();
    private final JTextField rowDelimiter = new JTextField();
    private final JCheckBox titles = new JCheckBox();

    public CsvOptionsEditor(){
        addField("Column delimiter", columnDelimiter);
        addField("Row delimiter", rowDelimiter);
        addField("Headers on first row", titles);
    }

    public void readOptions(CsvOptions options){
        columnDelimiter.setText(options.getColumnDelimiter());
        rowDelimiter.setText(options.getRowDelimiter());
        titles.setSelected(options.isTitles());
    }

    public void writeOptions(CsvOptions options){
        options.setColumnDelimiter(columnDelimiter.getText());
        options.setRowDelimiter(rowDelimiter.getText());
        options.setTitles(titles.isSelected());
    }

    public void setEnabled(boolean enabled){
        super.setEnabled(enabled);
        columnDelimiter.setEnabled(enabled);
        rowDelimiter.setEnabled(enabled);
        titles.setEnabled(enabled);
    }

    public void enableForType(DumpType type) {
        columnDelimiter.setEnabled(type == DumpType.CSV);
        rowDelimiter.setEnabled(type == DumpType.CSV);
    }
}
