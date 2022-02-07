package io.datakitchen.ide.ui;

import com.intellij.util.ui.JBUI;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

public class PasswordRenderer extends JPasswordField implements TableCellRenderer {
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        if (table == null){
            return this;
        }
        setMargin(JBUI.emptyInsets());
        if (value != null) {
            setText((String) value);
        } else {
            setText("");
        }
        if (isSelected){
            setForeground(table.getSelectionForeground());
            setBackground(table.getSelectionBackground());
        } else {
            setForeground(table.getForeground());
            setBackground(table.getBackground());
        }

        return this;
    }
}
