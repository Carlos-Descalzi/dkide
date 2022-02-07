package io.datakitchen.ide.dialogs;

import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import io.datakitchen.ide.model.NodeType;
import io.datakitchen.ide.ui.FormPanel;
import io.datakitchen.ide.ui.NodeTypeListCellRenderer;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ChangeNodeTypeDialog extends DialogWrapper {

    private final ComboBox<NodeType> nodeType = new ComboBox<>();

    public ChangeNodeTypeDialog(){
        this(null);
    }

    public ChangeNodeTypeDialog(String selectedNodeType){
        super(true);
        setTitle("Change Node Type");

        DefaultComboBoxModel<NodeType> typesModel = new DefaultComboBoxModel<>(NodeType.ALL_TYPES.toArray(NodeType[]::new));

        if (selectedNodeType != null) {
            for (int i = 0; i < typesModel.getSize(); i++) {
                if (typesModel.getElementAt(i).getTypeName().equals(selectedNodeType)) {
                    nodeType.setSelectedIndex(i);
                }
            }
            nodeType.setEnabled(false);
        }
        init();
    }
    @Override
    protected @NotNull List<ValidationInfo> doValidateAll() {
        List<ValidationInfo> validations = new ArrayList<>();

        if (nodeType.getSelectedItem() == null){
            validations.add(new ValidationInfo("Node type is required", nodeType));
        }

        return validations;
    }

    public String getNodeType(){
        return nodeType.getItem().getTypeName();
    }

    protected JComponent createCenterPanel(){

        FormPanel panel = new FormPanel();
        panel.addField("Node type", nodeType);

        nodeType.setPreferredSize(new Dimension(300,28));
        nodeType.setRenderer(new NodeTypeListCellRenderer());
        panel.add(nodeType);

        return panel;
    }

}
