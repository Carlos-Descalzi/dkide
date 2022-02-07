package io.datakitchen.ide.tools.orders;

import io.datakitchen.ide.platform.OrderRun;
import io.datakitchen.ide.ui.LineBorder;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class OrderListCellRenderer extends DefaultTableCellRenderer {
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

        Object displayValue;

        if (value instanceof OrderRun.OrderStatus){
            displayValue = ((OrderRun.OrderStatus)value).getDisplayName();
        } else if (value instanceof OrderRun.OrderRunStatus){
            displayValue = ((OrderRun.OrderRunStatus)value).getDisplayName();
        } else {
            displayValue = value;
        }

        JLabel l = (JLabel) super.getTableCellRendererComponent(table, displayValue, isSelected, hasFocus, row, column);

        if (column == 0 || column == 5){
            l.setFont(new Font("Monospaced", Font.PLAIN, getFont().getSize()-1));
        }
        if (column == 3 || column == 4 || column == 7){
            l.setHorizontalAlignment(JLabel.CENTER);
        } else {
            l.setHorizontalAlignment(JLabel.LEFT);
        }

        OrderRun orderRun = ((OrderListTableModel) table.getModel()).getOrderRun(row);

        boolean top = false;
        boolean right = false;
        boolean left = false;
        boolean bottom = false;

        if (row > 0) {
            OrderRun prev = ((OrderListTableModel) table.getModel()).getOrderRun(row - 1);
            if (!orderRun.getOrderId().equals(prev.getOrderId())) {
                top = true;
            } else if (column >= 0 && column <= 4) {
                l.setText("");
            }
        }

        if (column == 0 || column >= 5) {
            left = true;
        }
        if (column == 7) {
            right = true;
        }

        if (row == table.getRowCount() - 1) {
            bottom = true;
        }

        l.setBorder(new LineBorder(table.getBackground().brighter(), left, right, top, bottom));


        return l;
    }

}
