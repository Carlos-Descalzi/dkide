package io.datakitchen.ide.ui;

import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBScrollPane;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FiltrableList extends JPanel {

    private final EventSupport<ActionListener> eventSupport = EventSupport.of(ActionListener.class);
    private final JTextField filter = new JTextField();
    private final JList list = new JBList();
    private final JBScrollPane scroll = new JBScrollPane(list, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    private final DocumentListener listener = new DocumentAdapter() {
        @Override
        protected void textChanged(@NotNull DocumentEvent e) {
            update();
        }
    };

    private List<String> items = new ArrayList<>();

    public FiltrableList(){
        setLayout(new BorderLayout());
        add(filter, BorderLayout.NORTH);
        add(scroll,BorderLayout.CENTER);
        filter.getDocument().addDocumentListener(listener);
        filter.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                onKeyPressed(e);
            }
        });
        list.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                optionSelected();
            }
        });
    }

    private void onKeyPressed(KeyEvent e) {
        switch(e.getKeyCode()){
            case KeyEvent.VK_UP:
                moveSelection(-1);
                break;
            case KeyEvent.VK_DOWN:
                moveSelection(1);
                break;
            case KeyEvent.VK_ENTER:
                if (list.getSelectedIndex() != -1){
                    optionSelected();
                }
                break;
        }
    }

    private void moveSelection(int m){
        int index = list.getSelectedIndex() + m;
        if (index >= 0 && index < list.getModel().getSize()) {
            list.setSelectedIndex(index);
            scroll.getVerticalScrollBar().setValue(list.getCellBounds(index,index).y);
        }
    }

    private void optionSelected(){
        eventSupport.getProxy().actionPerformed(new ActionEvent(this,ActionEvent.ACTION_FIRST,""));
    }

    private void selectItem(ActionEvent e) {
        eventSupport.getProxy().actionPerformed(new ActionEvent(this,ActionEvent.ACTION_FIRST,""));
    }

    public void setList(List<String> items){
        filter.getDocument().removeDocumentListener(listener);
        filter.setText("");
        this.items = items;
        update();
        filter.getDocument().addDocumentListener(listener);
    }
    private void update(){
        String filterString = filter.getText().trim();
        Stream<String> stream = items.stream().sorted();

        if (!filterString.equals("")) {
            stream = stream.filter((String s)->{ return s.contains(filterString);});
        }
        list.setModel(new ListListModel(stream.collect(Collectors.toList())));
        if (list.getModel().getSize() > 0) {
            list.setSelectedIndex(0);
        }
        scroll.getVerticalScrollBar().setValue(0);
    }

    public void addActionListener(ActionListener listener){
        eventSupport.addListener(listener);
    }
    public void removeActionListener(ActionListener listener){
        eventSupport.removeListener(listener);
    }

    public String getValue() {
        return (String)list.getSelectedValue();
    }

    public JComponent getFilterField() {
        return filter;
    }
}
