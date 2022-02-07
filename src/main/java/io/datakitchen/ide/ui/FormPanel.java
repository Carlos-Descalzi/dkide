package io.datakitchen.ide.ui;

import javax.swing.*;
import java.awt.*;

public class FormPanel extends JPanel {

    private Dimension defaultFieldDimension;

    private Dimension labelDimension;

    public FormPanel(Dimension defaultFieldDimension, Dimension labelDimension){
        this.defaultFieldDimension = defaultFieldDimension;
        this.labelDimension = labelDimension;
        setLayout(new FormLayout(5,5));
    }

    public FormPanel(Dimension defaultFieldDimension){
        this(defaultFieldDimension, new Dimension(150,28));
    }


    public FormPanel(){
        this(new Dimension(200,28), new Dimension(150,28));
    }

    public void setDefaultFieldDimension(Dimension defaultFieldDimension) {
        this.defaultFieldDimension = defaultFieldDimension;
    }

    public void setLabelDimension(Dimension labelDimension) {
        this.labelDimension = labelDimension;
    }

    public void addField(String label, JComponent component, Dimension dimension){
        JLabel l = new JLabel(label);
        l.setFocusable(false);
        l.setPreferredSize(labelDimension);
        l.setLabelFor(component);
        component.setPreferredSize(dimension);
        add(l);
        add(component);
    }
    public void addField(String label, JComponent component){
        addField(label, component, defaultFieldDimension);
    }
}
