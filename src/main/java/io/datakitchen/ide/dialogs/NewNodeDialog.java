package io.datakitchen.ide.dialogs;

import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import io.datakitchen.ide.model.NodeType;
import io.datakitchen.ide.ui.FormPanel;
import io.datakitchen.ide.ui.NodeTypeListCellRenderer;
import io.datakitchen.ide.ui.RegExValidatedField;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class NewNodeDialog extends DialogWrapper {

    private final RegExValidatedField name = new RegExValidatedField(RegExValidatedField.IDENTIFIER);
    private final ComboBox<NodeType> nodeType = new ComboBox<>();

    public NewNodeDialog(){
        this(null);
    }

    public NewNodeDialog(String selectedNodeType){
        super(true);

        nodeType.setModel(new DefaultComboBoxModel<>(NodeType.ALL_TYPES.toArray(NodeType[]::new)));

        if (selectedNodeType != null) {
            nodeType.setSelectedItem(NodeType.getByTypeName(selectedNodeType));
            nodeType.setEnabled(false);
        }
        init();
    }
    @Override
    protected @NotNull java.util.List<ValidationInfo> doValidateAll() {
        List<ValidationInfo> validations = new ArrayList<>();

        if (StringUtils.isBlank(name.getText())){
            validations.add(new ValidationInfo("Name is required", name));
        }

        if (nodeType.getSelectedItem() == null){
            validations.add(new ValidationInfo("Node type is required", nodeType));
        }

        return validations;
    }

    public String getNodeName(){
        return name.getText();
    }

    public String getNodeType(){
        return nodeType.getItem().getTypeName();
    }

    protected JComponent createCenterPanel(){
        FormPanel panel = new FormPanel();
        panel.addField("Node name",name,new Dimension(300,28));
        panel.addField("Node type",nodeType,new Dimension(300,28));
        nodeType.setRenderer(new NodeTypeListCellRenderer());

        return panel;
    }

}
