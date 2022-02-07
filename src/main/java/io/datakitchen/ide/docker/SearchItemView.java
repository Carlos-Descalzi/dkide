package io.datakitchen.ide.docker;

import com.github.dockerjava.api.model.SearchItem;
import com.intellij.openapi.wm.impl.welcomeScreen.BottomLineBorder;
import com.intellij.util.ui.JBUI;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class SearchItemView extends JPanel {
    private final JLabel title = new JLabel();
    private final JTextArea area = new JTextArea();
    private final JLabel bottom = new JLabel();

    public SearchItemView(SearchItem item){
        setOpaque(true);
        setLayout(new BorderLayout());
        title.setText(item.getName());
        title.setFont(getFont().deriveFont(Font.BOLD));
        add(title, BorderLayout.NORTH);

        area.setOpaque(false);
        area.setText(item.getDescription());
        add(area, BorderLayout.CENTER);
        area.setPreferredSize(new Dimension(100,50));
        area.setBorder(JBUI.Borders.emptyLeft(10));
        setBorder(new BottomLineBorder());


        List<String> info = new ArrayList<>();

        if (item.isOfficial()){
            info.add("Official");
        }
        if (item.isTrusted() != null && item.isTrusted()){
            info.add("Trusted");
        }
        if (item.getStarCount() != null){
            info.add(item.getStarCount()+" stars");
        }
        bottom.setText(String.join(", ",info));
        bottom.setHorizontalAlignment(JLabel.RIGHT);
        bottom.setFont(getFont().deriveFont(getFont().getSize()-1f));
        add(bottom, BorderLayout.SOUTH);
    }

    public void setEnabled(boolean enabled){
        super.setEnabled(enabled);
        title.setEnabled(enabled);
        area.setEnabled(enabled);
        bottom.setEnabled(enabled);
    }
}
