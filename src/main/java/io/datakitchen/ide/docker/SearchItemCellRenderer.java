package io.datakitchen.ide.docker;

import com.github.dockerjava.api.model.SearchItem;

import javax.swing.*;
import java.awt.*;

public class SearchItemCellRenderer implements ListCellRenderer {
    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        SearchItem item = (SearchItem) value;
        SearchItemView view = new SearchItemView(item);
        view.setEnabled(list.isEnabled());

        if (isSelected) {
            view.setBackground(list.getSelectionBackground());
            view.setForeground(list.getSelectionForeground());
        } else {
            view.setBackground(list.getBackground());
            view.setForeground(list.getForeground());
        }

        return view;
    }
}
