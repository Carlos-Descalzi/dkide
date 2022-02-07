package io.datakitchen.ide.ui;

import com.intellij.openapi.util.IconLoader;
import io.datakitchen.ide.model.NodeType;

import javax.swing.*;
import java.awt.*;

public class NodeTypeListCellRenderer extends DefaultListCellRenderer {
    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        if (value != null) {
            String iconName = ((NodeType) value).getName();
            ((JLabel) c).setIcon(IconLoader.getIcon("/icons/" + iconName + ".svg", getClass()));
        }
        return c;
    }
}
