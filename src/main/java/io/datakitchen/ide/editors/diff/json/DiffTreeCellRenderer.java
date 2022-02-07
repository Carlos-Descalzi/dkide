package io.datakitchen.ide.editors.diff.json;

import javax.swing.*;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;
import java.util.List;
import java.util.Map;

class DiffTreeCellRenderer extends DefaultTreeCellRenderer {
    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {

        JLabel l = (JLabel) super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
        l.setIcon(null);
        l.setBorder(null);
        if (value == tree.getModel().getRoot()) {
            l.setText("Document");
        } else if (value instanceof KeyValuePair) {
            KeyValuePair kv = (KeyValuePair) value;

            String text;

            if (kv.getValue() instanceof List || kv.getValue() instanceof Map) {
                text = (String)kv.getKey();
            } else {
                text = kv.getKey()+": "+formatValue(kv.getValue());

                if (kv instanceof Patch){
                    text = decoratePatch((Patch)kv, text);
                }
            }
            l.setText("<html>"+text+"</html>");
        } else if (value instanceof KeyValuePairList) {
            l.setText("(items:)");
        } else if (value instanceof List) {
            String asStr = String.valueOf(value);
            if (asStr.length() > 25) {
                asStr = asStr.substring(0, 25) + "...";
            }
            l.setText(asStr);
        } else {
            String text = formatValue(value);

            if (value instanceof Patch){
                text = decoratePatch((Patch)value, text);
            }
            l.setText("<html>"+text+"</html>");
        }

        return l;
    }

    private String decoratePatch(Patch patch, String text){
        if (!patch.isDone()){
            String operation = patch.getOperation();

            if (Patch.OP_ADD.equals(operation)){
                return "<i><span color=\"#88FF88\">"+text+"</span></i>";
            } else if (Patch.OP_REPLACE.equals(operation)){
                return "<i><span color=\"#8888FF\">"+text+"</span></i>";
            } else if (Patch.OP_REMOVE.equals(operation)){
                return "<i><span color=\"#FF8888\">"+text+"</span></i>";
            }
        }
        return text;
    }

    private String formatValue(Object value){
        if (value instanceof Value){
            value = ((Value)value).getValue();
        } else if (value instanceof Patch){
            value = ((Patch)value).getValue();
        }
        if (value instanceof String) {
            return "<b>\"" + value + "\"</b>";
        }
        return "<b>"+value+"</b>";
    }


}
