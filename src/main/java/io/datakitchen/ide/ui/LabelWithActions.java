package io.datakitchen.ide.ui;

import com.intellij.openapi.util.IconLoader;
import com.intellij.util.ui.JBUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;

public class LabelWithActions extends JPanel {

    @FunctionalInterface
    public interface ActionSupplier<T extends LabelWithActions> {
        Action[] getActions(T requester);
    }

    private final Icon arrowIcon = IconLoader.getIcon("/arrow-down.png",getClass());
    private final ActionSupplier actionSupplier;
    private final List<Action> actions = new ArrayList<>();
    protected final JLabel label;
    private boolean highlightOnHover = true;
    private Action doubleClickAction;

    public LabelWithActions(String text, Icon icon, ActionSupplier actionSupplier){
        this(text, JLabel.LEFT, icon, actionSupplier);
    }

    public LabelWithActions(String text, int alignment, Icon icon, ActionSupplier actionSupplier){
        this.label = new JLabel(text){
            @Override
            public boolean contains(int x, int y) {
                return false;
            }
        };
        this.label.setHorizontalAlignment(alignment);
        this.label.setIcon(icon);
        this.label.setPreferredSize(new Dimension(200,28));
        this.actionSupplier = actionSupplier;
        enableEvents(AWTEvent.MOUSE_EVENT_MASK|AWTEvent.MOUSE_MOTION_EVENT_MASK);
        setLayout(new BorderLayout());
        JButton button = new JButton();
        button.setMargin(JBUI.emptyInsets());
        button.setPreferredSize(new Dimension(18,18));
        button.setContentAreaFilled(false);
        button.setBorder(null);
        button.setFocusable(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        add(label, BorderLayout.CENTER);
        add(button, BorderLayout.EAST);
        setBorder(JBUI.Borders.emptyBottom(1));

        MouseListener listener = new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setIcon(arrowIcon);
                if (highlightOnHover) {
                    setBorder(LineBorder.bottom());
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setIcon(null);
                if (highlightOnHover) {
                    setBorder(JBUI.Borders.emptyBottom(1));
                }
            }

            @Override
            public void mouseClicked(MouseEvent e){
                if (e.getButton() == 1
                    && e.getClickCount() == 2
                    && doubleClickAction != null){
                    doubleClickAction.actionPerformed(
                        new ActionEvent(LabelWithActions.this, ActionEvent.ACTION_PERFORMED, ""));
                }
            }
        };
        addMouseListener(listener);
        label.addMouseListener(listener);
        button.addMouseListener(listener);
        button.addActionListener((ActionEvent e)->{showOptions(button);});
    }

    private void showOptions(JComponent owner) {
        List<Action> actions = new ArrayList<>();
        actions.addAll(List.of(actionSupplier.getActions(this)));
        actions.addAll(this.actions);
        if (!actions.isEmpty()) {
            JPopupMenu popup = new JPopupMenu();
            for (Action action : actions) {
                popup.add(action);
            }
            popup.show(owner, 0, owner.getHeight());
        }
    }

    public void addActions(List<Action> actions){
        this.actions.addAll(actions);
    }

    public boolean isHighlightOnHover() {
        return highlightOnHover;
    }

    public void setHighlightOnHover(boolean highlightOnHover) {
        this.highlightOnHover = highlightOnHover;
    }

    public void setText(String newValue) {
        label.setText(newValue);
    }

    public void setIcon(Icon icon) {
        label.setIcon(icon);
    }

    public Action getDoubleClickAction() {
        return doubleClickAction;
    }

    public void setDoubleClickAction(Action doubleClickAction) {
        this.doubleClickAction = doubleClickAction;
    }
}
