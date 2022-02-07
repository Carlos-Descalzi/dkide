package io.datakitchen.ide.ui;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * A Layout for developing forms.
 * This layout puts the components in de next form:<br/>
 * label1 - component1<br/>
 * label2 - component2<br/>
 * It uses the atribute labelFor of the JLabel to recognize wich component is shown with each lbel.
 * @author <a href="mailto:cdescalzi2001@yahoo.com.ar">Carlos E.Descalzi</a>
 */
public class FormLayout implements LayoutManager {

    /**
     * The maximum label width, if is 0 will be adjusted automatically
     */
    private int labelSectionWidth;
    /**
     * The label alignment
     */
    private HorizontalAlignment labelAlignment;
    /**
     * The horizontal gap
     */
    private int hGap;
    /**
     * The vertical gap
     */
    private int vGap;

    public FormLayout(){
        this(0,0);
    }

    public FormLayout(int hGap,int vGap){
        this(hGap,vGap,HorizontalAlignment.LEFT,0);
    }

    public FormLayout(int hGap,int vGap,HorizontalAlignment labelAlignment,int labelSectionWidth){
        this.hGap = hGap;
        this.vGap = vGap;
        this.labelAlignment = labelAlignment;
        this.labelSectionWidth = labelSectionWidth;
    }

    public void addLayoutComponent(String name, Component comp) {
    }

    public void removeLayoutComponent(Component comp) {
    }

    public Dimension preferredLayoutSize(Container parent) {
        return minimumLayoutSize(parent);
    }

    public Dimension minimumLayoutSize(Container parent) {
        List<JLabel> labels = getLabels(parent.getComponents());

        Insets insets = parent.getInsets();

        int yOffset = insets.top;
        int minX = 0;
        int minY = 0;

        int labelSectionWidthUsed = 0;

        if (labelSectionWidth == 0){
            for (JLabel label:labels){
                if (label.getPreferredSize().width > labelSectionWidthUsed){
                    labelSectionWidthUsed = label.getPreferredSize().width;
                }
            }
        } else {
            labelSectionWidthUsed = labelSectionWidth;
        }


        for (JLabel label:labels){
            Dimension labelPreferredSize = label.getPreferredSize();

            int x = insets.left+ (labelAlignment.equals(HorizontalAlignment.LEFT) ? 0 :
                    (labelAlignment.equals(HorizontalAlignment.CENTER) ?
                            (labelSectionWidthUsed - labelPreferredSize.width)/2 :
                            labelSectionWidthUsed - labelPreferredSize.width));

            int width = x;

            if (minX < width){
                minX = width;
            }
            Component labelled = label.getLabelFor();

            Dimension preferredSize = labelled.getPreferredSize();

            width = labelSectionWidthUsed+hGap+insets.left+insets.right+preferredSize.width;
            if (minX < width){
                minX = width;
            }


            yOffset+=vGap + (preferredSize.height > labelPreferredSize.height ?
                    preferredSize.height : labelPreferredSize.height);
        }
        minY = yOffset+insets.bottom;

        return new Dimension(minX,minY);
    }

    public void layoutContainer(Container parent) {
        List<JLabel> labels = getLabels(parent.getComponents());

        Insets insets = parent.getInsets();

        int yOffset = insets.top;

        int labelSectionWidthUsed = 0;

        if (labelSectionWidth == 0){
            for (JLabel label:labels){
                if (label.getPreferredSize().width > labelSectionWidthUsed){
                    labelSectionWidthUsed = label.getPreferredSize().width;
                }
            }
        } else {
            labelSectionWidthUsed = labelSectionWidth;
        }
        for (JLabel label:labels){
            Dimension labelPreferredSize = label.getPreferredSize();

            int x = insets.left+ (labelAlignment.equals(HorizontalAlignment.LEFT) ? 0 :
                    (labelAlignment.equals(HorizontalAlignment.CENTER) ?
                            (labelSectionWidthUsed - labelPreferredSize.width)/2 :
                            labelSectionWidthUsed - labelPreferredSize.width));

            label.setBounds(
                    x,
                    yOffset,
                    labelSectionWidthUsed,
                    labelPreferredSize.height);
            Component labelled = label.getLabelFor();

            Dimension preferredSize = labelled.getPreferredSize();

            labelled.setBounds(
                    labelSectionWidthUsed+hGap+insets.left,
                    yOffset,
                    preferredSize.width,
                    preferredSize.height
            );

            yOffset+=vGap + (preferredSize.height > labelPreferredSize.height ?
                    preferredSize.height : labelPreferredSize.height);
        }
    }

    private List<JLabel> getLabels(Component[] components){
        List<JLabel> labels = new ArrayList<JLabel>();

        for (Component component:components){
            if (component instanceof JLabel &&
                    ((JLabel)component).getLabelFor() != null){
                labels.add(((JLabel)component));
            }
        }


        return labels;
    }

    public int getLabelSectionWidth() {
        return labelSectionWidth;
    }

    public void setLabelSectionWidth(int labelSectionWidth) {
        this.labelSectionWidth = labelSectionWidth;
    }

    public HorizontalAlignment getLabelAlignment() {
        return labelAlignment;
    }

    public void setLabelAlignment(HorizontalAlignment labelAlignment) {
        this.labelAlignment = labelAlignment;
    }

    public int getHGap() {
        return hGap;
    }

    public void setHGap(int gap) {
        hGap = gap;
    }

    public int getVGap() {
        return vGap;
    }

    public void setVGap(int gap) {
        vGap = gap;
    }

    /**
     * Convenience method to add a component with its label to a container.
     * It will create a label component and bind it to the component.
     * @param component
     * @param label
     * @param container
     */
    public FormLayout add(Component component,String label,Container container){
        return add(component,new JLabel(label),container);
    }
    /**
     * Convenience method to add a component with its label to a container.
     * @param component
     * @param label
     * @param container
     */
    public FormLayout add(Component component,JLabel label,Container container){
        label.setLabelFor(component);
        container.add(label);
        container.add(component);
        return this;
    }

}