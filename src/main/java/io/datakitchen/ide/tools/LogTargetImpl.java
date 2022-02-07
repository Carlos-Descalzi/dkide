package io.datakitchen.ide.tools;

import com.intellij.ui.components.JBScrollPane;

import javax.swing.*;
import javax.swing.text.Document;
import java.awt.*;

public class LogTargetImpl extends JPanel implements LogTarget{
    private final JTextPane textPane = new JTextPane();

    public LogTargetImpl(){
        setLayout(new BorderLayout());
        add(new JBScrollPane(textPane,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED),BorderLayout.CENTER);
    }

    public void println(String message){
        SwingUtilities.invokeLater(()->{
            try {
                Document document = textPane.getDocument();
                document.insertString(document.getLength(), message+"\n", null);
            }catch (Exception ex){
                ex.printStackTrace();
            }
        });
    }

}
