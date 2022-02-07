package io.datakitchen.ide.config.editors;

import io.datakitchen.ide.HelpMessages;
import io.datakitchen.ide.config.MiscOptions;
import io.datakitchen.ide.ui.FormPanel;
import io.datakitchen.ide.ui.HelpContainer;

import javax.swing.*;
import java.awt.*;

public class MiscOptionsEditor extends FormPanel {

    private final JCheckBox enableCustomForms = new JCheckBox();
    private final JCheckBox enableCustomNodeForms = new JCheckBox();
    private final JCheckBox enableCustomDsForms = new JCheckBox();
    private final JCheckBox hideInactiveNodes = new JCheckBox();
    private final JCheckBox hideConfigJsonOnForms = new JCheckBox();
    private final JCheckBox simplifiedView = new JCheckBox();

    public MiscOptionsEditor(){
        super(new Dimension(200,28),new Dimension(250,28));

        addField("Enable custom forms" ,new HelpContainer(enableCustomForms, HelpMessages.CUSTOM_FORMS_MSG), new Dimension(40,28));
        addField("Node custom forms" ,new HelpContainer(enableCustomNodeForms, HelpMessages.CUSTOM_NODE_FORMS), new Dimension(40,28));
        addField("Source/Sink custom forms" ,new HelpContainer(enableCustomDsForms, HelpMessages.CUSTOM_DS_FORMS), new Dimension(40,28));
        addField("Hide config.json when using forms" ,new HelpContainer(hideConfigJsonOnForms, HelpMessages.CONFIG_JSON_MSG), new Dimension(40,28));
        addField("Hide inactive nodes" ,new HelpContainer(hideInactiveNodes, HelpMessages.HIDE_NODES_MSG), new Dimension(40,28));
        addField("Use simplified node views", new HelpContainer(simplifiedView, HelpMessages.CONFIG_SIMPLIFIED_VIEW_MSG), new Dimension(40,28));
    }

    public void setMiscOptions(MiscOptions miscOptions) {
        if (miscOptions != null) {
            enableCustomForms.setSelected(miscOptions.isUseCustomForms());
            enableCustomNodeForms.setSelected(miscOptions.isCustomNodeFormsEnabled());
            enableCustomDsForms.setSelected(miscOptions.isCustomDsFormsEnabled());
            hideInactiveNodes.setSelected(miscOptions.isHideInactiveNodes());
            hideConfigJsonOnForms.setSelected(miscOptions.isHideConfigJsonOnForms());
            simplifiedView.setSelected(miscOptions.isSimplifiedView());
        }
    }

    public MiscOptions getMiscOptions() {
        MiscOptions options = new MiscOptions();
        options.setUseCustomForms(enableCustomForms.isSelected());
        options.setCustomNodeFormsEnabled(enableCustomNodeForms.isSelected());
        options.setCustomDsFormsEnabled(enableCustomDsForms.isSelected());
        options.setHideInactiveNodes(hideInactiveNodes.isSelected());
        options.setHideConfigJsonOnForms(hideConfigJsonOnForms.isSelected());
        options.setSimplifiedView(simplifiedView.isSelected());
        return options;
    }
}
