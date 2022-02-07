package io.datakitchen.ide.ui;

import javax.swing.*;
import java.awt.*;

public class ButtonsBar extends JPanel {
    public ButtonsBar(Action ... actions){
        setLayout(new FlowLayout(FlowLayout.RIGHT));
        for (Action action:actions){
            addAction(action);
        }
    }

    public void addAction(Action action){
        add(new JButton(action));
    }
}
