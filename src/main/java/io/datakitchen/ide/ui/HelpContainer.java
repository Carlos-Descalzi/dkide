package io.datakitchen.ide.ui;

import com.intellij.icons.AllIcons;

import javax.swing.*;
import java.awt.*;

public class HelpContainer extends JPanel {

    public HelpContainer(JComponent component, String helpMessage){
        setLayout(new BorderLayout());
        add(component, BorderLayout.CENTER);
        JLabel helpIcon = new JLabel(AllIcons.General.ContextHelp);
        add(helpIcon, BorderLayout.EAST);
        helpIcon.setToolTipText(helpMessage);
    }
}
