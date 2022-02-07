package io.datakitchen.ide.config.editors;

import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.JBTable;
import io.datakitchen.ide.config.Secret;
import io.datakitchen.ide.ui.PasswordRenderer;
import io.datakitchen.ide.ui.SimpleAction;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;

public class SecretListEditor extends JPanel {

    private final SecretsTableModel model = new SecretsTableModel();
    private final JTable table = new JBTable();

    private final Action addSecretAction = new SimpleAction("Add",this::addSecret);
    private final Action removeSecretAction = new SimpleAction("Remove",this::removeSecret);

    public SecretListEditor(){
        setLayout(new BorderLayout());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(new JButton(addSecretAction));
        buttons.add(new JButton(removeSecretAction));
        table.setAutoCreateColumnsFromModel(false);

        table.getColumnModel().addColumn(new TableColumn(0,400, new DefaultTableCellRenderer(), new DefaultCellEditor(new JTextField())));
        table.getColumnModel().addColumn(new TableColumn(1,300, new PasswordRenderer(), new SecretEditor()));
        table.setModel(model);
        table.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        add(new JBScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED),BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);
        table.getSelectionModel().addListSelectionListener((ListSelectionEvent e)->{updateActions();});

        updateActions();
    }

    private void addSecret(ActionEvent e){
        model.addSecret();
        updateActions();
    }

    public void removeSecret(ActionEvent e){
        int[] indices = table.getSelectionModel().getSelectedIndices();
        model.removeSecrets(indices);
        updateActions();
    }

    private void updateActions(){
        removeSecretAction.setEnabled(table.getSelectionModel().getSelectedIndices().length > 0);
    }

    public void setEnabled(boolean enabled){
        super.setEnabled(enabled);
        table.setEnabled(enabled);
        addSecretAction.setEnabled(enabled);
        removeSecretAction.setEnabled(enabled && table.getSelectionModel().getSelectedIndices().length > 0);
    }

    public List<Secret> getSecrets(){
        return model.getSecrets();
    }

    public void setSecrets(List<Secret> secrets){
        model.setSecrets(secrets);
        updateActions();
    }
}
