package io.datakitchen.ide.editors.diff.json;

import javax.swing.*;
import java.awt.*;

public class DiffLayout implements LayoutManager {
    private final JComponent leftPanel;
    private final JComponent rightPanel;
    private final JComponent centerPanel;

    public DiffLayout(JComponent leftPanel, JComponent centerPanel, JComponent rightPanel) {
        this.leftPanel = leftPanel;
        this.centerPanel = centerPanel;
        this.rightPanel = rightPanel;
    }

    @Override
    public void addLayoutComponent(String name, Component comp) {
    }

    @Override
    public void removeLayoutComponent(Component comp) {
    }

    @Override
    public Dimension preferredLayoutSize(Container parent) {
        Dimension d = new Dimension();
        d.width += leftPanel.getPreferredSize().width;
        d.width += rightPanel.getPreferredSize().width;
        d.width += 20;
        d.height = Math.max(leftPanel.getPreferredSize().height, rightPanel.getPreferredSize().height);
        return d;
    }

    @Override
    public Dimension minimumLayoutSize(Container parent) {
        return new Dimension(100, 100);
    }

    @Override
    public void layoutContainer(Container parent) {
        int width = parent.getWidth();
        int height = parent.getHeight();

        int paneWidth = (width - 20) / 2;
        leftPanel.setBounds(0, 0, paneWidth, height);
        centerPanel.setBounds(paneWidth, 0, 20, height);
        rightPanel.setBounds(paneWidth + 20, 0, paneWidth, height);
    }
}
