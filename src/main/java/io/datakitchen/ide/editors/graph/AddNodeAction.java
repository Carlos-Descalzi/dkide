package io.datakitchen.ide.editors.graph;

import com.intellij.openapi.util.IconLoader;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class AddNodeAction extends AbstractAction {
    private String nodeName;
    private final boolean existingNode;
    private final ActionListener listener;
    public AddNodeAction(String nodeName, String description, boolean existingNode, ActionListener listener){
        super(description);
        this.existingNode = existingNode;
        String iconName = nodeName;
        if (iconName.contains(".")){
            iconName = iconName.split("\\.")[0];
        }
        this.putValue(Action.SMALL_ICON, IconLoader.getIcon("/icons/"+iconName+".svg",getClass()));
        this.nodeName = nodeName;
        this.listener = listener;
    }

    public boolean isExistingNode() {
        return existingNode;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        listener.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, nodeName));
    }
}
