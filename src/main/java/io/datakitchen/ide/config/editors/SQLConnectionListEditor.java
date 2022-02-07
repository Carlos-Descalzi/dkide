package io.datakitchen.ide.config.editors;

import com.intellij.util.ui.JBUI;
import io.datakitchen.ide.editors.DocumentChangeEvent;
import io.datakitchen.ide.editors.DocumentChangeListener;
import io.datakitchen.ide.tools.DatabaseConfiguration;
import io.datakitchen.ide.ui.ItemList;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class SQLConnectionListEditor extends JPanel implements DocumentChangeListener {
    private final ItemList<DatabaseConfiguration> itemList = new ItemList<>(this::createConfiguration);
    private final SQLConnectionEditor connectionEditor = new SQLConnectionEditor();
    private DatabaseConfiguration currentItem;

    public SQLConnectionListEditor(){
        setLayout(new BorderLayout());
        add(itemList, BorderLayout.WEST);
        connectionEditor.setBorder(JBUI.Borders.empty(10));
        add(connectionEditor, BorderLayout.CENTER);
        itemList.addListSelectionListener(this::itemSelected);
        connectionEditor.addDocumentChangeListener(this);
    }

    private DatabaseConfiguration createConfiguration(){
        return new DatabaseConfiguration("connection-"+(itemList.getDataSize()+1));
    }

    private void itemSelected(ListSelectionEvent listSelectionEvent) {
        if (this.currentItem != null){
            connectionEditor.save(currentItem);
        }
        currentItem = itemList.getSelected();
        connectionEditor.load(currentItem);
    }

    @Override
    public void documentChanged(DocumentChangeEvent e) {
        if (currentItem != null){
            connectionEditor.save(currentItem);
        }
    }

    public List<DatabaseConfiguration> getConnections(){
        return itemList.getData();
    }

    public void setConnections(List<DatabaseConfiguration> connections){
        this.itemList.setData(connections == null ? new ArrayList<>() : connections);
    }

    public void setEnabled(boolean enabled){
        super.setEnabled(enabled);
        itemList.setEnabled(enabled);
        connectionEditor.setEnabled(enabled);
    }

}
