package io.datakitchen.ide.editors.overrides;

import com.intellij.openapi.util.IconLoader;
import io.datakitchen.ide.Constants;
import io.datakitchen.ide.editors.neweditors.EditorIcons;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class OverrideRenderer extends DefaultTableCellRenderer {
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        LocalOverride override = (LocalOverride) value;

        JLabel renderer = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        renderer.setText(override.getName());

        if (override.getTypeName() != null &&
                override.getTypeName().startsWith(Constants.SCHEMA_PREFIX)){

            String connectorName = override.getTypeName().replace(Constants.SCHEMA_PREFIX,"").replace("Config","");

            renderer.setIcon(IconLoader.getIcon("/icons/connectors/"+connectorName+"_small.svg",getClass()));
        } else {
            renderer.setIcon(EditorIcons.VARIABLE);
        }

        return renderer;
    }

}
