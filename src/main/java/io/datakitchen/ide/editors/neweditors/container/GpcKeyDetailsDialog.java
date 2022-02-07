package io.datakitchen.ide.editors.neweditors.container;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTabbedPane;
import com.intellij.ui.table.JBTable;
import io.datakitchen.ide.json.CustomJsonParser;
import io.datakitchen.ide.ui.ButtonsBar;
import io.datakitchen.ide.ui.EditorUtil;
import io.datakitchen.ide.ui.SimpleAction;
import io.datakitchen.ide.util.JsonUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class GpcKeyDetailsDialog extends DialogWrapper {

    private final Editor parametersEditor;
    private final JTable environmentTable = new JBTable();
    private final JTable exportsTable = new JBTable();
    private final Action addEnvironmentAction = new SimpleAction("Add",this::addEnvironment);
    private final Action removeEnvironmentAction = new SimpleAction("Remove", this::removeEnvironment);
    private final Action addExportAction = new SimpleAction("Add", this::addExport);
    private final Action removeExportAction = new SimpleAction("Remove", this::removeExport);
    private final TableModelListener tableListener = e -> updateActions();


    public GpcKeyDetailsDialog(Project project) {
        super(true);
        setTitle("GPC Key Details");
        parametersEditor = EditorUtil.createJsonEditor(project);
        init();
    }

    @Override
    protected void dispose() {
        super.dispose();
        EditorFactory.getInstance().releaseEditor(parametersEditor);
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        JTabbedPane tabs = new JBTabbedPane();
        JPanel parametersPanel = createParametersPanel();
        JPanel environmentPanel = createEnvironmentPanel();
        environmentTable.setModel(new DefaultTableModel(new String[]{"Name", "Value"},0));

        JPanel exportsPanel = new JPanel(new BorderLayout());
        exportsTable.setModel(new DefaultTableModel(new String[]{"Variable"},0));
        exportsPanel.add(new JBScrollPane(exportsTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED), BorderLayout.CENTER);
        exportsPanel.add(new ButtonsBar(addExportAction, removeExportAction), BorderLayout.SOUTH);

        tabs.addTab("Parameters", parametersPanel);
        tabs.addTab("Environment", environmentPanel);
        tabs.addTab("Exported variables",exportsPanel);
        tabs.setPreferredSize(new Dimension(600,500));
        environmentTable.getModel().addTableModelListener(tableListener);
        exportsTable.getModel().addTableModelListener(tableListener);
        parametersEditor.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void documentChanged(@NotNull DocumentEvent event) {
                updateActions();
            }
        });
        environmentTable.getSelectionModel().addListSelectionListener(e -> updateActions());
        exportsTable.getSelectionModel().addListSelectionListener(e -> updateActions());
        updateActions();
        return tabs;
    }

    @NotNull
    private JPanel createEnvironmentPanel() {
        JPanel environmentPanel = new JPanel(new BorderLayout());
        environmentPanel.add(new JBScrollPane(environmentTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED), BorderLayout.CENTER);
        environmentPanel.add(new ButtonsBar(addEnvironmentAction, removeEnvironmentAction), BorderLayout.SOUTH);
        return environmentPanel;
    }

    @NotNull
    private JPanel createParametersPanel() {
        JPanel parametersPanel = new JPanel(new BorderLayout());
        parametersPanel.add(parametersEditor.getComponent());
        return parametersPanel;
    }


    private void addEnvironment(ActionEvent event) {
        ((DefaultTableModel)environmentTable.getModel()).addRow(new String[]{"",""});
        updateActions();
    }
    private void removeEnvironment(ActionEvent event) {
        ((DefaultTableModel)environmentTable.getModel()).removeRow(environmentTable.getSelectedRow());
        updateActions();
    }
    private void addExport(ActionEvent event) {
        ((DefaultTableModel) exportsTable.getModel()).addRow(new String[]{""});
        updateActions();
    }
    private void removeExport(ActionEvent event) {
        ((DefaultTableModel) exportsTable.getModel()).removeRow(exportsTable.getSelectedRow());
        updateActions();
    }


    private void updateActions(){
        removeEnvironmentAction.setEnabled(environmentTable.getSelectedRow() != -1);
        removeExportAction.setEnabled(exportsTable.getSelectedRow() != -1);
    }

    public void setParameters(Map<String, Object> parameters){
        if (parameters != null) {
            EditorUtil.setText(parametersEditor, JsonUtil.toJsonString(parameters));
        } else {
            EditorUtil.setText(parametersEditor, "{\n}\n");
        }
    }

    public void setEnvironment(Map<String, String> environment){
        if (environment != null) {
            DefaultTableModel model = (DefaultTableModel) environmentTable.getModel();
            for (Map.Entry<String, String> entry : environment.entrySet()) {
                model.addRow(new Object[]{entry.getKey(), entry.getValue()});
            }
        }
    }

    public void setExports(List<String> exports){
        if (exports != null) {
            DefaultTableModel model = (DefaultTableModel) exportsTable.getModel();

            for (String pipPackage : exports) {
                model.addRow(new String[]{pipPackage});
            }
        }
    }

    public Map<String, Object> getParameters(){
        try {
            return CustomJsonParser.parse(parametersEditor.getDocument().getText());
        } catch (Exception ex){
            return new LinkedHashMap<>();
        }

    }

    public Map<String, String> getEnvironment(){
        Map<String, String> environment = new LinkedHashMap<>();

        DefaultTableModel model = (DefaultTableModel) environmentTable.getModel();

        for (int i=0;i<model.getRowCount();i++){
            environment.put((String)model.getValueAt(i,0), (String)model.getValueAt(i,1));
        }

        return environment;
    }

    public List<String> getExports(){
        List<String> result = new ArrayList<>();

        DefaultTableModel model = (DefaultTableModel) exportsTable.getModel();

        for (int i=0;i<model.getRowCount();i++){
            String name = (String)model.getValueAt(i, 0);
            result.add(name);
        }

        return result;
    }

}
