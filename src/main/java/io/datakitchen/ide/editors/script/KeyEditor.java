package io.datakitchen.ide.editors.script;

import com.intellij.openapi.ui.ComboBox;
import io.datakitchen.ide.editors.DsInfo;

import javax.swing.*;
import java.awt.*;

public class KeyEditor extends DefaultCellEditor {
    private final int sourceColumn;

    public KeyEditor(int sourceColumn) {
        super(new ComboBox<>());
        this.sourceColumn = sourceColumn;
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        DsInfo dsInfo = (DsInfo) table.getValueAt(row, sourceColumn);

        ComboBox comboBox = (ComboBox) super.getTableCellEditorComponent(table, value, isSelected, row, column);

        if (dsInfo != null) {
            comboBox.setModel(new DefaultComboBoxModel(dsInfo.getKeys().toArray()));
        } else {
            comboBox.setModel(new DefaultComboBoxModel());
        }

        return comboBox;
    }
}
