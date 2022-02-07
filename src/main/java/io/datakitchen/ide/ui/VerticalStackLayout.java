package io.datakitchen.ide.ui;

import java.awt.*;

public class VerticalStackLayout implements LayoutManager {

    private final int padding;

    public VerticalStackLayout(){
        this(0);
    }

    public VerticalStackLayout(int padding){
        this.padding = padding;
    }

    @Override
    public void addLayoutComponent(String name, Component comp) {
    }

    @Override
    public void removeLayoutComponent(Component comp) {
    }

    @Override
    public Dimension preferredLayoutSize(Container parent) {
        Insets insets = parent.getInsets();
        Dimension d = new Dimension(0,insets.top+insets.bottom);
        for (Component c:parent.getComponents()){
            Dimension cd = getSize(c);
            d.width = Math.max(d.width,cd.width);
            d.height+= cd.height;
        }
        d.height += padding * Math.max(parent.getComponentCount()-1,0);

        d.width+=insets.left+insets.right;

        return d;
    }

    private Dimension getSize(Component c){
        Dimension pd = c.getPreferredSize();
        Dimension md = c.getMinimumSize();
        return new Dimension(pd.width, Math.max(pd.height, md.height));
    }

    @Override
    public Dimension minimumLayoutSize(Container parent) {
        Insets insets = parent.getInsets();
        Dimension d = new Dimension(0,insets.top+insets.bottom);
        for (Component c:parent.getComponents()){
            Dimension cd = c.getMinimumSize();
            d.width = Math.max(d.width,cd.width);
            d.height+= cd.height;
        }
        d.width+=insets.left+insets.right;
        if (parent.isMinimumSizeSet()){
            d.width = Math.max(parent.getMinimumSize().width,d.width);
        }
        return d;
    }

    @Override
    public void layoutContainer(Container parent) {
        Insets insets = parent.getInsets();
        int y = insets.top;
        Component[] components = parent.getComponents();
        for (int i=0;i<components.length;i++){
            if (i > 0){
                y+=padding;
            }
            Component c = components[i];
            Dimension d = getSize(c);
            c.setBounds(insets.left,y,parent.getWidth()-(insets.left+insets.right),d.height);
            y+=d.height;
        }
    }
}
