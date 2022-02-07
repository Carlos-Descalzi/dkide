package io.datakitchen.ide.ui;

import javax.swing.*;
import java.awt.*;

public class CompoundPanel extends JPanel {

    public CompoundPanel(JComponent frontPanel, JComponent backPanel){
        frontPanel.setOpaque(false);
        add(frontPanel);
        add(backPanel);
        setLayout(new LayoutManager() {
            @Override
            public void addLayoutComponent(String name, Component comp) {}

            @Override
            public void removeLayoutComponent(Component comp) {}

            @Override
            public Dimension preferredLayoutSize(Container parent) {
                return backPanel.getPreferredSize();
            }

            @Override
            public Dimension minimumLayoutSize(Container parent) {
                return null;
            }

            @Override
            public void layoutContainer(Container parent) {
                Rectangle r = new Rectangle(0,0,getWidth(),getHeight());
                frontPanel.setBounds(r);
                backPanel.setBounds(r);
            }
        });
    }

}