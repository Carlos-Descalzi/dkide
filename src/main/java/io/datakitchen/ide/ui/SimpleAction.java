package io.datakitchen.ide.ui;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SimpleAction extends AbstractAction {
    private final ActionListener listener;

    public SimpleAction(String name, ActionListener listener){
        super(name);
        this.listener = listener;
    }

    public SimpleAction(Icon icon, String tooltip, ActionListener listener){
        super();
        putValue(Action.SMALL_ICON,icon);
        putValue(Action.SHORT_DESCRIPTION, tooltip);
        this.listener = listener;
    }

    public SimpleAction(Icon icon, String text, String tooltip, ActionListener listener){
        super();
        putValue(Action.SMALL_ICON,icon);
        putValue(Action.NAME,text);
        putValue(Action.SHORT_DESCRIPTION, tooltip);
        this.listener = listener;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        listener.actionPerformed(e);
    }
}
