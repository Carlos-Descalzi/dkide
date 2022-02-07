package io.datakitchen.ide.config.editors;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.util.ui.JBUI;
import io.datakitchen.ide.ui.SimpleAction;
import org.apache.commons.io.FileUtils;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.File;
import java.io.IOException;

public class SecretEditor extends AbstractCellEditor implements TableCellEditor {

    private static final Logger LOGGER = Logger.getInstance(SecretEditor.class);

    private final JPanel panel = new JPanel();
    private final JPasswordField field = new JPasswordField();
    private Object value;
    private String oldValue;

    public SecretEditor(){
        panel.setLayout(new BorderLayout());
        panel.add(field, BorderLayout.CENTER);
        JPanel buttons = new JPanel(new GridLayout(1,2));
        Action pasteAction = new SimpleAction(AllIcons.Actions.MenuPaste, "", "Paste from clipboard", this::paste);
        buttons.add(makeButton(pasteAction));
        Action loadFromFileAction = new SimpleAction(AllIcons.FileTypes.Any_type, "", "Load from file", this::loadFromFile);
        buttons.add(makeButton(loadFromFileAction));
        panel.add(buttons, BorderLayout.EAST);
        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e){
                oldValue = String.valueOf(field.getPassword());
            }
            @Override
            public void focusLost(FocusEvent e) {
                String newValue = String.valueOf(field.getPassword());
                if (!oldValue.equals(newValue)){
//                    System.out.println("value changed "+oldValue+","+newValue);
                    value = newValue;
                } else {
//                    System.out.println("value not changed");
                }
            }
        });
        field.addActionListener(e -> {
            value = String.valueOf(field.getPassword());
            oldValue = String.valueOf(field.getPassword());
        });
    }

    private JButton makeButton(Action action){
        JButton button = new JButton(action);
        button.setContentAreaFilled(false);
        button.setBorder(null);
        button.setMargin(JBUI.emptyInsets());
        button.setPreferredSize(new Dimension(18,18));
        return button;
    }

    private void paste(ActionEvent event) {
        Transferable transferable = panel.getToolkit().getSystemClipboard().getContents(this);

        try {
            value = transferable.getTransferData(DataFlavor.stringFlavor);
            String newValue = String.valueOf(value);
            field.setText(newValue);
            oldValue = newValue;
        }catch(UnsupportedFlavorException| IOException ignored){}
    }

    private void loadFromFile(ActionEvent event) {
        JFileChooser chooser = new JFileChooser();

        if (chooser.showOpenDialog(panel) == JFileChooser.APPROVE_OPTION){
            File file = chooser.getSelectedFile();

            try {
                value = FileUtils.readFileToString(file, "utf-8");
                String newValue = String.valueOf(value);
                field.setText(newValue);
                oldValue = newValue;
            }catch(IOException ex){
                LOGGER.error(ex);
            }
        }
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        this.value = value;
        if (value == null){
            oldValue = "";
            field.setText("");
        } else {
            field.setText(String.valueOf(value));
            oldValue = String.valueOf(value);
        }
        return panel;
    }

    @Override
    public Object getCellEditorValue() {
        return value;
    }
}
