package io.datakitchen.ide.editors.neweditors.container;

import io.datakitchen.ide.editors.neweditors.ConnectionListView;
import io.datakitchen.ide.editors.neweditors.ConnectionView;
import io.datakitchen.ide.editors.neweditors.KeyView;
import io.datakitchen.ide.model.Connection;
import io.datakitchen.ide.ui.SimpleAction;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.function.Consumer;

public class DataSinkFileConnectionView extends ConnectionView {
    private final JButton addFileButton;
    private final Consumer<Connection> onAddFile;
    public DataSinkFileConnectionView(
            ConnectionListView connectionListView,
            Connection connection,
            Consumer<Connection> onAddFile) {
        super(connectionListView, connection);
        addFileButton = new JButton(new SimpleAction("+ Add file", this::addFile));
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
