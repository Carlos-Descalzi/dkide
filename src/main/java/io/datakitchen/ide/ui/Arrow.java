package io.datakitchen.ide.ui;

import org.apache.commons.lang.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Path2D;

public class Arrow extends JComponent {

    private final String action;

    public Arrow(String action){
        this.action = action;
        setPreferredSize(new Dimension(50,100));
    }

    protected void paintComponent(Graphics g){
        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(getForeground());
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int h = getHeight();
        int hh = h/2;
        int w = getWidth();

        Path2D.Float path = new Path2D.Float();
        path.moveTo(5, hh+5);
        path.lineTo(5,hh-5);
        path.lineTo(w-30,hh-5);
        path.lineTo(w-30,hh-15);
        path.lineTo(w-5,hh);
        path.lineTo(w-30,hh+15);
        path.lineTo(w-30,hh+5);
        path.lineTo(5,hh+5);
        g2d.draw(path);

        if (StringUtils.isNotBlank(action)) {
            FontMetrics fm = getFontMetrics(getFont());

            g2d.drawString(action, (w - fm.stringWidth(action)) / 2, fm.getHeight() + fm.getDescent());
        }
    }
}
