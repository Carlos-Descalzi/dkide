package io.datakitchen.ide.editors.sql;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.components.JBTabbedPane;
import com.intellij.util.ui.JBUI;
import io.datakitchen.ide.editors.DocumentChangeEvent;
import io.datakitchen.ide.editors.DocumentChangeListener;
import io.datakitchen.ide.editors.DocumentEditor;
import io.datakitchen.ide.ui.EditorUtil;
import io.datakitchen.ide.ui.EventSupport;
import io.datakitchen.ide.ui.FieldListener;
import io.datakitchen.ide.ui.FormPanel;
import io.datakitchen.ide.util.ObjectUtil;
import io.datakitchen.ide.util.RecipeUtil;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.*;

public class SQLSinkKeyEditor extends JPanel implements DocumentEditor, Disposable {
    private final EventSupport<DocumentChangeListener> eventSupport = EventSupport.of(DocumentChangeListener.class);
    protected ComboBox<String> loadType = new ComboBox<>();
    private final JTextField tableName = new JTextField();
    private final ComboBox<String> format = new ComboBox<>(new DefaultComboBoxModel<>(new String[]{"binary","csv","json"}));
    private final ComboBox<String> resources = new ComboBox<>();
    private final JTextField colDelimiter = new JTextField();
    private final JTextField rowDelimiter = new JTextField();
    private final JCheckBox headers = new JCheckBox();
    private final JTextField rowCount = new JTextField();
    private final JTextField columnCount= new JTextField();
    private Editor tableDdl;
    private Editor dmlTemplate;

    private final FieldListener listener = new FieldListener(this::notifyUpdate);

    private final Module module;

    public SQLSinkKeyEditor(Module module){
        this(module,new String[0]);
    }

    public SQLSinkKeyEditor(Module module, String[] additionalInsertTypes){
        this.module = module;
        setLayout(new BorderLayout());
        List<String> insertTypes = new ArrayList<>(Arrays.asList("bulk_insert","execute_dml"));
        insertTypes.addAll(Arrays.asList(additionalInsertTypes));
        loadType.setModel(new DefaultComboBoxModel<>(insertTypes.toArray(String[]::new)));

        JTabbedPane tabsTop = new JBTabbedPane();

        tabsTop.add("General", buildGeneralOptions());
        tabsTop.add("Format", buildOutputFormatOptions());
        tabsTop.add("Runtime Variables", buildRuntimeVariables());

        add(tabsTop,BorderLayout.NORTH);

        listener.listen(dmlTemplate);
        listener.listen(tableDdl);
        listener.listen(loadType);
        listener.listen(format);
        listener.listen(colDelimiter);
        listener.listen(rowDelimiter);
        listener.listen(headers);

        enableEvents();
        updateActions();
    }
    private void enableEvents() {
        listener.setEnabled(true);
    }
    private void disableEvents() {
        listener.setEnabled(false);
    }

    public void addDocumentChangeListener(DocumentChangeListener listener){
        eventSupport.addListener(listener);
    }

    public void removeDocumentChangeListener(DocumentChangeListener listener){
        eventSupport.removeListener(listener);
    }

    @Override
    public JComponent getEditorComponent() {
        return this;
    }

    protected JPanel buildGeneralOptions(){
        FormPanel topPanel = new FormPanel();

        topPanel.addField("Load type", loadType);
        topPanel.addField("Table name", tableName, new Dimension(350,28));

        try {
            tableDdl = EditorUtil.createSqlEditor(module.getProject());
            JComponent editorComponent = tableDdl.getComponent();
            topPanel.addField("Table DDL", editorComponent, new Dimension(400,300));
        } catch (Exception ex){
            ex.printStackTrace();
        }
        try {
            dmlTemplate = EditorUtil.createSqlEditor(module.getProject());
            JComponent editorComponent = dmlTemplate.getComponent();
            topPanel.addField("Insert Sentence", editorComponent, new Dimension(400,300));
        } catch (Exception ex){
            ex.printStackTrace();
        }

        topPanel.setBorder(JBUI.Borders.empty(10));

        return topPanel;
    }

    @Override
    public void dispose() {
        EditorFactory.getInstance().releaseEditor(tableDdl);
        EditorFactory.getInstance().releaseEditor(dmlTemplate);
    }
    public void reloadResources(){
        VirtualFile resourcesFolder = RecipeUtil.recipeFolder(module).findChild("resources");

        if (resourcesFolder != null){
            this.resources.setModel(
                    new DefaultComboBoxModel<>(
                        Arrays.stream(resourcesFolder.getChildren())
                        .map((VirtualFile f) -> "resources/" + f.getName()).toArray(String[]::new)
                )
            );
        }
    }

    protected JPanel buildOutputFormatOptions(){
        FormPanel panel = new FormPanel();

        panel.addField("Input format",format,new Dimension(200,28));
        panel.addField("Column delimiter",colDelimiter,new Dimension(30,28));
        panel.addField("Row delimiter",rowDelimiter,new Dimension(30,28));
        panel.addField("Data has headers",headers,new Dimension(30,28));

        panel.setBorder(JBUI.Borders.empty(10));
        format.addActionListener((ActionEvent e)-> updateActions());

        return panel;
    }
    protected JPanel buildRuntimeVariables(){
        FormPanel runtimeVariables = new FormPanel();
        runtimeVariables.setBorder(JBUI.Borders.empty(10));

        runtimeVariables.addField("Row count",rowCount,new Dimension(200,28));
        runtimeVariables.addField("Column count",columnCount,new Dimension(200,28));


        return runtimeVariables;
    }

    private void notifyUpdate() {
        System.out.println("Key changed");
        updateActions();
        eventSupport.getProxy().documentChanged(new DocumentChangeEvent(this));
    }

    private void updateActions(){
        boolean isCsv = "csv".equals(this.format.getSelectedItem());
        rowDelimiter.setEnabled(isCsv);
        colDelimiter.setEnabled(isCsv);
    }
    public void saveKey(SqlKey key){
        String tableName = this.tableName.getText();
        if (StringUtils.isNotBlank(tableName)) {
            key.getContent().put("table-name", tableName);
        } else {
            key.getContent().remove("table-name");
        }
        String tableDdl = this.tableDdl.getDocument().getText();
        if (StringUtils.isNotBlank(tableDdl)){
            key.getContent().put("table-ddl",tableDdl);
        } else {
            key.getContent().remove("table-ddl");
        }
        String dmlTemplate = this.dmlTemplate.getDocument().getText();
        if (StringUtils.isNotBlank(dmlTemplate)){
            key.getContent().put("insert-template", dmlTemplate);
        } else {
            key.getContent().remove("insert-template");
        }
        key.getContent().put("query-type",loadType.getSelectedItem());
        key.getContent().put("format",format.getSelectedItem());

        String colDelimiter = this.colDelimiter.getText();
        if (StringUtils.isNotBlank(colDelimiter)){
            key.getContent().put("col-delimiter",colDelimiter);
        }
        String rowDelimiter = this.rowDelimiter.getText();
        if (StringUtils.isNotBlank(rowDelimiter)){
            key.getContent().put("row-delimiter",rowDelimiter);
        }
        if (headers.isSelected()){
            key.getContent().put("first-row-column-names",true);
        }
        Map<String, Object> runtimeVars = new LinkedHashMap<>();

        String rowCount = this.rowCount.getText();
        if (StringUtils.isNotBlank(rowCount)) {
            runtimeVars.put("row_count", rowCount);
        }
        String columnCount = this.columnCount.getText();
        if (StringUtils.isNotBlank(columnCount)){
            runtimeVars.put("column_count",columnCount);
        }
        if (!runtimeVars.isEmpty()){
            key.getContent().put("set-runtime-vars",runtimeVars);
        }
    }
    public void loadKey(SqlKey key) {
        disableEvents();
        tableName.setText(StringUtils.defaultString((String) key.getContent().get("table-name"), ""));
        EditorUtil.setText(tableDdl, StringUtils.defaultString((String) key.getContent().get("table-ddl"), ""));
        EditorUtil.setText(dmlTemplate, StringUtils.defaultString((String) key.getContent().get("insert-template")));
        loadType.setSelectedItem(key.getContent().get("query-type"));
        format.setSelectedItem(StringUtils.defaultString((String) key.getContent().get("format"), "binary"));

        Map<String, Object> options = ObjectUtil.cast(key.getContent().get("options"));

        if (options != null){
            colDelimiter.setText(StringUtils.defaultString((String) options.get("col-delimiter"), ""));
            rowDelimiter.setText(StringUtils.defaultString((String) options.get("row-delimiter"), ""));
            headers.setSelected(Boolean.TRUE.equals(options.get("first-row-column-names")));
        } else {
            colDelimiter.setText(StringUtils.defaultString((String) key.getContent().get("col-delimiter"), ""));
            rowDelimiter.setText(StringUtils.defaultString((String) key.getContent().get("row-delimiter"), ""));
            headers.setSelected(Boolean.TRUE.equals(key.getContent().get("first-row-column-names")));
        }

        Map<String, Object> runtimeVars = ObjectUtil.cast(key.getContent().get("set-runtime-vars"));

        if (runtimeVars != null){
            rowCount.setText((String)runtimeVars.getOrDefault("row_count",""));
            columnCount.setText((String)runtimeVars.getOrDefault("column_count",""));
        } else {
            rowCount.setText("");
            columnCount.setText("");
        }

        enableEvents();
        updateActions();
    }
    public void clear() {
        tableName.setText("");
        EditorUtil.setText(tableDdl, "");
        EditorUtil.setText(dmlTemplate, "");
        loadType.setSelectedIndex(0);
        format.setSelectedIndex(0);
        colDelimiter.setText("");
        rowDelimiter.setText("");
        headers.setSelected(false);
        rowCount.setText("");
        columnCount.setText("");
    }

}
