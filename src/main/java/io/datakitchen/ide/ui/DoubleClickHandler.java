package io.datakitchen.ide.ui;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;

public class DoubleClickHandler extends MouseAdapter {

    private final Consumer<MouseEvent> target;

    public DoubleClickHandler(Consumer<MouseEvent> target){
        this.target = target;
    }

    public void mouseClicked(MouseEvent e){
        if (e.getButton() == MouseEvent.BUTTON1
            && e.getClickCount() == 2){
            target.accept(e);
        }
    }
}
