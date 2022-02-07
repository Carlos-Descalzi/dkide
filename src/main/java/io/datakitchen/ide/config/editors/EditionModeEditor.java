package io.datakitchen.ide.config.editors;

import io.datakitchen.ide.config.MiscOptions;

import javax.swing.*;
import javax.swing.text.html.HTMLEditorKit;
import java.awt.*;

public class EditionModeEditor extends JPanel {

    private final JRadioButton simplifiedView = new JRadioButton("Use Simplified View");
    private final JRadioButton advancedView = new JRadioButton("Use Advanced View");

    public EditionModeEditor(){
        setLayout(new BorderLayout());


        JPanel simplifiedViewPanel = new JPanel(new BorderLayout());
        JEditorPane description = new JEditorPane();
        description.setEditorKit(new HTMLEditorKit());
        description.setOpaque(false);
        description.setEditable(false);
        description.setText("<html><ul style=\"font-family:"+getFont().getFamily()+"\">"
            +"<li>All settings of a node are configurable in one same editor</li>"
            +"<li>Some settings of node configuration are hidden to user and automatically configured</li>"
            +"<li>In project tree, nodes appear as a single tree node</li>"
            +"</ul></html>");
        simplifiedViewPanel.add(simplifiedView, BorderLayout.NORTH);
        simplifiedViewPanel.add(description, BorderLayout.CENTER);


        JPanel advancedViewPanel = new JPanel(new BorderLayout());
        description = new JEditorPane();
        description.setEditorKit(new HTMLEditorKit());
        description.setOpaque(false);
        description.setEditable(false);
        description.setText("<html><ul style=\"font-family:"+getFont().getFamily()+"\">"
            +"<li>Allows finer edition of nodes, data sources and data sinks</li>"
            +"<li>All entities of a node are displayed and edited separately</li>"
            +"<li>Allows work with Jinja test expressions</li>"
            +"</ul></html>");
        advancedViewPanel.add(advancedView, BorderLayout.NORTH);
        advancedViewPanel.add(description, BorderLayout.CENTER);

        setLayout(new GridLayout(2,1));
        add(simplifiedViewPanel);
        add(advancedViewPanel);

        ButtonGroup group = new ButtonGroup();
        group.add(simplifiedView);
        group.add(advancedView);
        simplifiedView.setSelected(true);
    }

    private MiscOptions configuration;

    public void setConfiguration(MiscOptions miscOptions) {
        this.configuration = miscOptions;
    }

    public MiscOptions getConfiguration() {
        if (configuration == null){
            configuration = new MiscOptions();
        }
        configuration.setSimplifiedView(simplifiedView.isSelected());
        return configuration;
    }
}
