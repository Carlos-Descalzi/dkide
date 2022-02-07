package io.datakitchen.ide.ui;

import com.intellij.util.ui.JBUI;

import javax.swing.border.Border;
import java.awt.*;

public class LineBorder implements Border {

    private final Color color;
    private final boolean left;
    private final boolean right;
    private final boolean top;
    private final boolean bottom;
    private final Insets insets;

    private static final LineBorder TOP = new LineBorder( false, false, true, false);
    private static final LineBorder BOTTOM = new LineBorder( false, false, false, true);
    private static final LineBorder LEFT = new LineBorder( true, false, false, false);
    private static final LineBorder RIGHT = new LineBorder( false, true, false, false);

    public static LineBorder top(Color color){
        return new LineBorder(color, false, false, true, false);
    }
    public static LineBorder bottom(Color color){
        return new LineBorder(color, false, false, false, true);
    }
    public static LineBorder left(Color color){
        return new LineBorder(color, true, false, false, false);
    }
    public static LineBorder right(Color color){
        return new LineBorder(color, false, true, false, false);
    }

    public static LineBorder top(){
        return TOP;
    }
    public static LineBorder bottom(){
        return BOTTOM;
    }
    public static LineBorder left(){
        return LEFT;
    }
    public static LineBorder right(){
        return RIGHT;
    }

    public LineBorder(boolean left, boolean right, boolean top, boolean bottom){
        this(null,left, right, top, bottom);
    }

    public LineBorder(Color color, boolean left, boolean right, boolean top, boolean bottom){
        this.color = color;
        this.left = left;
        this.right = right;
        this.top = top;
        this.bottom = bottom;

        insets = JBUI.insets(top ? 1 : 0, left ? 1 : 0, bottom ? 1 : 0, right ? 1 : 0);
    }

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        g.setColor(color != null ? color : c.getForeground());

        if (bottom) {
            g.drawLine(x, y + height - 1, x + width, y + height - 1);
        }
        if (top){
            g.drawLine(x, y , x + width, y );
        }
        if (left){
            g.drawLine(x,y,x,y+height-1);
        }
        if (right){
            g.drawLine(x+width-1,y,x+width-1,y+height-1);
        }
    }

    @Override
    public Insets getBorderInsets(Component c) {
        return insets;
    }

    @Override
    public boolean isBorderOpaque() {
        return false;
    }
}
