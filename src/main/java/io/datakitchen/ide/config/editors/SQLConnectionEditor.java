package io.datakitchen.ide.config.editors;

import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.JBTable;
import io.datakitchen.ide.dialogs.FileChooserDialog;
import io.datakitchen.ide.editors.DocumentChangeEvent;
import io.datakitchen.ide.editors.DocumentChangeListener;
import io.datakitchen.ide.editors.DocumentEditor;
import io.datakitchen.ide.tools.DatabaseConfiguration;
import io.datakitchen.ide.ui.EventSupport;
import io.datakitchen.ide.ui.FieldListener;
import io.datakitchen.ide.ui.FormPanel;
import io.datakitchen.ide.ui.SimpleAction;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SQLConnectionEditor extends FormPanel implements DocumentEditor {

    private final EventSupport<DocumentChangeListener> eventSupport = EventSupport.of(DocumentChangeListener.class);

    private final JTextField name = new JTextField();
    private final JTextField driverClassName = new JTextField();
    private final JTextField url = new JTextField();
    private final JTextField username = new JTextField();
    private final JPasswordField password = new JPasswordField();
    private final DefaultTableModel driverListModel = new DefaultTableModel(new String[]{"Driver jar"},0);
    private final JTable driverList = new JBTable(driverListModel);
    private final Action addDriverAction = new SimpleAction("Add",this::addDriver);
    private final Action removeDriverAction = new SimpleAction("Remove",this::removeDriver);
    private final FieldListener listener = new FieldListener(this::notifyChange);

    public SQLConnectionEditor(){
        addField("Name",name);
        addField("Driver class name",driverClassName);
        addField("JDBC url",url);
        addField("Username",username);
        addField("Password",password);

        JPanel drivers = new JPanel(new BorderLayout());
        drivers.add(new JBScrollPane(driverList,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(new JButton(addDriverAction));
        buttons.add(new JButton(removeDriverAction));
        drivers.add(buttons,BorderLayout.SOUTH);
        addField("Jars",drivers, new Dimension(300,100));
        updateActions();
        listener.listen(name);
        listener.listen(url);
        listener.listen(password);
        listener.listen(username);
        listener.listen(driverList);
        load(null);
    }

    private void addDriver(ActionEvent event) {

        FileChooserDialog dialog = new FileChooserDialog();
        dialog.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isDirectory() || f.getName().endsWith(".jar");
            }

            @Override
            public String getDescription() {
                return "Jar file";
            }
        });

        if (dialog.showAndGet()){
            File file = dialog.getSelectedFile();
            driverListModel.addRow(new String[]{file.getAbsolutePath()});
        }
        updateActions();
    }

    private void removeDriver(ActionEvent event) {
        int index = driverList.getSelectedRow();
        if (index != -1) {
            driverListModel.removeRow(index);
        }
        updateActions();
    }

    private void updateActions(){
        removeDriverAction.setEnabled(driverList.getSelectedRow() != -1);
    }


    public void load(DatabaseConfiguration config){
        listener.setEnabled(false);
        while (driverListModel.getRowCount() > 0) {
            driverListModel.removeRow(0);
        }
        if (config != null) {
            name.setText(config.getName());
            url.setText(config.getUrl());
            driverClassName.setText(config.getDriverClassName());
            username.setText(config.getUsername());
            password.setText(config.getPassword());
            for (String path : config.getDriverJarPaths()) {
                driverListModel.addRow(new String[]{path});
            }
            setEnabled(true);
        } else {
            name.setText("");
            url.setText("");
            username.setText("");
            driverClassName.setText("");
            password.setText("");
            setEnabled(false);
        }
        listener.setEnabled(true);
    }

    public void setEnabled(boolean enabled){
        name.setEnabled(enabled);
        url.setEnabled(enabled);
        driverClassName.setEnabled(enabled);
        username.setEnabled(enabled);
        password.setEnabled(enabled);
        driverList.setEnabled(enabled);
    }

    public void save(DatabaseConfiguration config){
        config.setName(name.getText());
        config.setDriverClassName(driverClassName.getText());
        config.setUrl(url.getText());
        config.setUsername(username.getText());
        config.setPassword(String.valueOf(password.getPassword()));
        List<String> paths = new ArrayList<>();
        for(int i=0;i<driverListModel.getRowCount();i++){
            paths.add((String)driverListModel.getValueAt(i,0));
        }
        config.setDriverJarPaths(paths);
    }

    private void notifyChange() {
        eventSupport.getProxy().documentChanged(new DocumentChangeEvent(this));
    }

    @Override
    public void addDocumentChangeListener(DocumentChangeListener listener) {
        eventSupport.addListener(listener);
    }

    @Override
    public void removeDocumentChangeListener(DocumentChangeListener listener) {
        eventSupport.removeListener(listener);
    }

    @Override
    public JComponent getEditorComponent() {
        return this;
    }
}
