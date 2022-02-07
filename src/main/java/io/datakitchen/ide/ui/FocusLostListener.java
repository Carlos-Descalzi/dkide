package io.datakitchen.ide.ui;

import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.function.Consumer;

public class FocusLostListener extends FocusAdapter {
    private final Consumer<FocusEvent> listener;
    public FocusLostListener(Consumer<FocusEvent> listener){
        this.listener = listener;
    }
    public void focusGained(FocusEvent e){}
    public void focusLost(FocusEvent e){ listener.accept(e);}

}
