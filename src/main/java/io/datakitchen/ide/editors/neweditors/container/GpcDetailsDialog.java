package io.datakitchen.ide.editors.neweditors.container;

import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.JBTable;
import io.datakitchen.ide.ui.ButtonsBar;
import io.datakitchen.ide.ui.SimpleAction;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

public class GpcDetailsDialog extends DialogWrapper {

    private final JTable pipDependenciesTable = new JBTable();
    private final JTable aptDependenciesTable = new JBTable();
    private final Action addPipPackageAction = new SimpleAction("Add", this::addPipPackage);
    private final Action removePipPackageAction = new SimpleAction("Remove", this::removePipPackage);
    private final Action addAptPackageAction = new SimpleAction("Add", this::addAptPackage);
    private final Action removeAptPackageAction = new SimpleAction("Remove", this::removeAptPackage);


    protected GpcDetailsDialog() {
        super(true);
        setTitle("Edit GPC Details");
        init();
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        JPanel panel = new JPanel(new GridLayout(2,1));
        JPanel pipDependenciesPanel = createPipDependenciesPanel();
        pipDependenciesPanel.setBorder(new TitledBorder("Pip dependencies"));
        JPanel aptDependenciesPanel = createAptDependenciesPanel();
        aptDependenciesPanel.setBorder(new TitledBorder("System Packages"));
        panel.add(pipDependenciesPanel);
        panel.add(aptDependenciesPanel);
        panel.setPreferredSize(new Dimension(500,500));
        pipDependenciesTable.getModel().addTableModelListener(tableListener);
        aptDependenciesTable.getModel().addTableModelListener(tableListener);
        pipDependenciesTable.getSelectionModel().addListSelectionListener(e -> updateActions());
        aptDependenciesTable.getSelectionModel().addListSelectionListener(e -> updateActions());
        updateActions();
        return panel;
    }


    private final TableModelListener tableListener = e -> updateActions();

    private JPanel createPipDependenciesPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        pipDependenciesTable.setModel(new DefaultTableModel(new String[]{"Name"},0));
        panel.add(new JBScrollPane(pipDependenciesTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED), BorderLayout.CENTER);
        panel.add(new ButtonsBar(addPipPackageAction, removePipPackageAction), BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createAptDependenciesPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JBScrollPane(aptDependenciesTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED), BorderLayout.CENTER);
        aptDependenciesTable.setModel(new DefaultTableModel(new String[]{"Name"},0));
        panel.add(new ButtonsBar(addAptPackageAction, removeAptPackageAction), BorderLayout.SOUTH);
        return panel;
    }

    private void addPipPackage(ActionEvent event) {
        ((DefaultTableModel)pipDependenciesTable.getModel()).addRow(new String[]{""});
        updateActions();
    }
    private void removePipPackage(ActionEvent event) {
        ((DefaultTableModel)pipDependenciesTable.getModel()).removeRow(pipDependenciesTable.getSelectedRow());
        updateActions();
    }
    private void addAptPackage(ActionEvent event) {
        ((DefaultTableModel)aptDependenciesTable.getModel()).addRow(new String[]{""});
        updateActions();
    }
    private void removeAptPackage(ActionEvent event) {
        ((DefaultTableModel)aptDependenciesTable.getModel()).removeRow(aptDependenciesTable.getSelectedRow());
        updateActions();
    }

    private void updateActions(){
        removePipPackageAction.setEnabled(pipDependenciesTable.getSelectedRow() != -1);
        removeAptPackageAction.setEnabled(aptDependenciesTable.getSelectedRow() != -1);
    }

    public void setPipPackages(List<String> pipPackages){
        if (pipPackages != null) {
            DefaultTableModel model = (DefaultTableModel) pipDependenciesTable.getModel();

            for (String pipPackage : pipPackages) {
                model.addRow(new String[]{pipPackage});
            }
        }
    }

    public void setAptPackage(List<String> aptPackages){
        if (aptPackages != null) {
            DefaultTableModel model = (DefaultTableModel) aptDependenciesTable.getModel();

            for (String aptPackage : aptPackages) {
                model.addRow(new String[]{aptPackage});
            }
        }
    }

    public List<String> getPipPackages(){
        List<String> result = new ArrayList<>();

        DefaultTableModel model = (DefaultTableModel) pipDependenciesTable.getModel();

        for (int i=0;i<model.getRowCount();i++){
            String name = (String)model.getValueAt(i, 0);
            result.add(name);
        }

        return result;
    }

    public List<String> getAptPackages(){
        List<String> result = new ArrayList<>();

        DefaultTableModel model = (DefaultTableModel) aptDependenciesTable.getModel();

        for (int i=0;i<model.getRowCount();i++){
            String name = (String)model.getValueAt(i, 0);
            result.add(name);
        }

        return result;
    }
}
