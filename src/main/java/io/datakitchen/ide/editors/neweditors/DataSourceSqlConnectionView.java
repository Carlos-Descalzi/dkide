package io.datakitchen.ide.editors.neweditors;

import io.datakitchen.ide.model.Connection;
import io.datakitchen.ide.ui.SimpleAction;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.function.Consumer;

public class DataSourceSqlConnectionView extends ConnectionView{

    private final JButton addFileButton;
    private final Consumer<Connection> onAddFile;

    public DataSourceSqlConnectionView(
            ConnectionListView connectionListView,
            Connection connection,
           Consumer<Connection> onAddFile) {
        super(connectionListView, connection);
        addFileButton = new JButton(new SimpleAction("+ Add Query file", this::addFile));
        Font font = getFont();
        addFileButton.setFont(font.deriveFont(font.getSize()-2f));
        addFileButton.setContentAreaFilled(false);
        addFileButton.setBorderPainted(false);
        addFileButton.setHorizontalAlignment(JButton.LEFT);
        keys.add(addFileButton);
        this.onAddFile = onAddFile;
    }

    private void addFile(ActionEvent event) {
        this.onAddFile.accept(this.getConnection());
    }

    @Override
    public Point getHookForNewFileEntry() {
        return addFileButton.getLocationOnScreen();
    }

    @Override
    protected void insertNewKey(KeyView keyView) {
        keys.add(keyView, keys.getComponentCount()-1);
        revalidate();
        repaint();
    }
}
