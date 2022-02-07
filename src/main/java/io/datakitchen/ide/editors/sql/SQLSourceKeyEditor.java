package io.datakitchen.ide.editors.sql;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.components.JBTabbedPane;
import com.intellij.util.ui.JBUI;
import io.datakitchen.ide.editors.DocumentChangeEvent;
import io.datakitchen.ide.editors.DocumentChangeListener;
import io.datakitchen.ide.editors.DocumentEditor;
import io.datakitchen.ide.ui.EventSupport;
import io.datakitchen.ide.ui.FieldListener;
import io.datakitchen.ide.ui.FormPanel;
import io.datakitchen.ide.ui.SQLKeyEditorField;
import io.datakitchen.ide.util.ObjectUtil;
import io.datakitchen.ide.util.RecipeUtil;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.*;

public class SQLSourceKeyEditor extends JPanel implements DocumentEditor, Disposable {
    private final EventSupport<DocumentChangeListener> eventSupport = EventSupport.of(DocumentChangeListener.class);
    protected ComboBox<String> queryType = new ComboBox<>();
    private final ComboBox<String> format = new ComboBox<>(new DefaultComboBoxModel<>(new String[]{"binary","csv","json"}));
    private final JRadioButton inlineQuery = new JRadioButton("Inline");
    private final JRadioButton resourceQuery = new JRadioButton("Resource File");
    private final ComboBox<String> resources = new ComboBox<>();
    private final JTextField colDelimiter = new JTextField();
    private final JTextField rowDelimiter = new JTextField();
    private final JCheckBox headers = new JCheckBox();
    private final JTextField rowCount = new JTextField();
    private final JTextField columnCount= new JTextField();
    private final JTextField result = new JTextField();
    private SQLKeyEditorField editor;

    private final FieldListener fieldListener = new FieldListener(this::notifyUpdate);

    private final Module module;

    public SQLSourceKeyEditor(Module module){
        this(module,new String[0]);
    }

    public SQLSourceKeyEditor(Module module, String[] additionalQueryTypes){
        this.module = module;
        setLayout(new BorderLayout());

        List<String> queryTypes = new ArrayList<>(
                List.of("execute_query","execute_scalar","execute_non_query")
        );
        queryTypes.addAll(List.of(additionalQueryTypes));

        queryType.setModel(new DefaultComboBoxModel<>(queryTypes.toArray(String[]::new)));

        JTabbedPane tabsTop = new JBTabbedPane();

        tabsTop.add("General", buildGeneralOptions());
        tabsTop.add("Format", buildOutputFormatOptions());
        tabsTop.add("Runtime Variables", buildRuntimeVariables());

        add(tabsTop,BorderLayout.NORTH);

        try {
            editor = new SQLKeyEditorField(module);// EditorUtil.createSqlEditor(module.getProject());
            Disposer.register(this, editor);
            add(editor,BorderLayout.CENTER);// add(editor.getComponent(),BorderLayout.CENTER);
        } catch (Exception ex){
            ex.printStackTrace();
        }
        fieldListener.listen(editor);
        fieldListener.listen(queryType);
        fieldListener.listen(format);
        fieldListener.listen(colDelimiter);
        fieldListener.listen(rowDelimiter);
        fieldListener.listen(headers);
        fieldListener.listen(rowCount);
        fieldListener.listen(columnCount);
        fieldListener.listen(result);
        fieldListener.listen(inlineQuery);
        fieldListener.listen(resourceQuery);

        enableEvents();
        updateActions();
    }

    @Override
    public void dispose() {
    }

    private void enableEvents() {
        fieldListener.setEnabled(true);
    }
    private void disableEvents() {
        fieldListener.setEnabled(false);
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

        topPanel.addField("Type",queryType);

        JPanel queryOptions = new JPanel(new FlowLayout(FlowLayout.LEFT));
        queryOptions.add(inlineQuery);
        queryOptions.add(resourceQuery);

        ButtonGroup group = new ButtonGroup();
        group.add(inlineQuery);
        group.add(resourceQuery);

        inlineQuery.setSelected(true);

        topPanel.addField("Query", queryOptions, new Dimension(500,28));
        topPanel.addField("Resource file", resources, new Dimension(300,28));
        topPanel.setBorder(JBUI.Borders.empty(10));

        return topPanel;
    }

    public void reloadResources(){
        VirtualFile resourcesFolder = RecipeUtil.recipeFolder(module).findChild("resources");

        if (resourcesFolder != null){
            this.resources.setModel(new DefaultComboBoxModel<>(
                    Arrays.stream(resourcesFolder.getChildren())
                    .map(VirtualFile::getName).toArray(String[]::new)));
        }
    }

    protected JPanel buildOutputFormatOptions(){
        FormPanel panel = new FormPanel();

        panel.addField("Format",format,new Dimension(200,28));
        panel.addField("Column delimiter",colDelimiter,new Dimension(30,28));
        panel.addField("Row delimiter",rowDelimiter,new Dimension(30,28));
        panel.addField("Write headers",headers,new Dimension(30,28));

        panel.setBorder(JBUI.Borders.empty(10));
        format.addActionListener((ActionEvent e)-> updateActions());

        return panel;
    }
    protected JPanel buildRuntimeVariables(){
        FormPanel runtimeVariables = new FormPanel();
        runtimeVariables.setBorder(JBUI.Borders.empty(10));

        runtimeVariables.addField("Row count",rowCount,new Dimension(200,28));
        runtimeVariables.addField("Column count",columnCount,new Dimension(200,28));
        runtimeVariables.addField("Result",result,new Dimension(200,28));


        return runtimeVariables;
    }

    private void notifyUpdate() {
        updateActions();
        eventSupport.getProxy().documentChanged(new DocumentChangeEvent(this));
    }

    public void setEnabled(boolean enabled){
        super.setEnabled(enabled);
        updateActions();
    }

    private void updateActions(){
        queryType.setEnabled(isEnabled());
        inlineQuery.setEnabled(isEnabled());
        resourceQuery.setEnabled(isEnabled());
        format.setEnabled(isEnabled());
        boolean isCsv = "csv".equals(this.format.getSelectedItem());
        rowDelimiter.setEnabled(isEnabled() && isCsv);
        colDelimiter.setEnabled(isEnabled() && isCsv);
        resources.setEnabled(isEnabled() && resourceQuery.isSelected());
        editor.setEnabled(isEnabled() && inlineQuery.isSelected());
        rowCount.setEnabled(isEnabled());
        columnCount.setEnabled(isEnabled());
        result.setEnabled(isEnabled());
    }
    public void clear() {
        editor.setText("");
        queryType.setSelectedIndex(0);
        inlineQuery.setSelected(true);
        format.setSelectedIndex(0);
        rowDelimiter.setText("");
        colDelimiter.setText("");
        if (resources.getModel().getSize() > 0) {
            resources.setSelectedIndex(0);
        }
        editor.setText("");
        rowCount.setText("");
        columnCount.setText("");
        result.setText("");
    }

    public void loadKey(SqlKey key){
        disableEvents();

        if (key != null) {
            queryType.setSelectedItem(key.getContent().get("query-type"));
            format.setSelectedItem(StringUtils.defaultString((String) key.getContent().get("format"), "binary"));

            String sqlFile = (String) key.getContent().get("sql-file");
            if (StringUtils.isNotBlank(sqlFile)) {
                resourceQuery.setSelected(true);
                resources.setSelectedItem(sqlFile);
                editor.setText("");
            } else {
                inlineQuery.setSelected(true);
                editor.setText(StringUtils.defaultString((String) key.getContent().get("sql"),""));
            }
            Map<String, Object> options = ObjectUtil.cast(key.getContent().get("options"));

            if (options != null) {
                colDelimiter.setText(StringUtils.defaultString((String) options.get("col-delimiter"), ""));
                rowDelimiter.setText(StringUtils.defaultString((String) options.get("row-delimiter"), ""));
                headers.setSelected(Boolean.TRUE.equals(options.get("first-row-column-names")));
            } else {
                colDelimiter.setText(StringUtils.defaultString((String) key.getContent().get("col-delimiter"), ""));
                rowDelimiter.setText(StringUtils.defaultString((String) key.getContent().get("row-delimiter"), ""));
                headers.setSelected(Boolean.TRUE.equals(key.getContent().get("first-row-column-names")));
            }

            Map<String, Object> runtimeVars = ObjectUtil.cast(key.getContent().get("set-runtime-vars"));

            if (runtimeVars != null) {
                columnCount.setText(StringUtils.defaultString((String) runtimeVars.get("column_count"), ""));
                rowCount.setText(StringUtils.defaultString((String) runtimeVars.get("row_count"), ""));
                result.setText(StringUtils.defaultString((String) runtimeVars.get("result"),""));
            } else {
                columnCount.setText("");
                rowCount.setText("");
                result.setText("");
            }
            setEnabled(true);
        } else {
            queryType.setSelectedItem(0);
            format.setSelectedIndex(0);
            inlineQuery.setSelected(true);
            editor.setText("");
            colDelimiter.setText("");
            rowDelimiter.setText("");
            headers.setSelected(false);
            columnCount.setText("");
            rowCount.setText("");
            result.setText("");
            setEnabled(false);
        }

        enableEvents();
    }

    public void saveKey(SqlKey key){
        Map<String, Object> content = new LinkedHashMap<>();
        content.put("query-type", queryType.getSelectedItem());
        content.put("format", format.getSelectedItem());

        if (inlineQuery.isSelected()){
            content.put("sql", editor.getText());
        } else {
            content.put("sql-file",resources.getSelectedItem());
        }

        String colDelimiter = this.colDelimiter.getText();
        if (StringUtils.isNotBlank(colDelimiter)){
            content.put("col-delimiter",colDelimiter);
        }
        String rowDelimiter = this.rowDelimiter.getText();
        if (StringUtils.isNotBlank(rowDelimiter)){
            content.put("row-delimiter",rowDelimiter);
        }
        if (headers.isSelected()){
            content.put("first-row-column-names",headers.isSelected());
        }

        Map<String, Object> runtimeVars = new LinkedHashMap<>();
        String columnCount = this.columnCount.getText();
        if (StringUtils.isNotBlank(columnCount)){
            runtimeVars.put("column_count", columnCount);
        }
        String rowCount = this.rowCount.getText();
        if (StringUtils.isNotBlank(rowCount)){
            runtimeVars.put("row_count",rowCount);
        }
        String result = this.result.getText();
        if (StringUtils.isNotBlank(result)){
            runtimeVars.put("result",result);
        }
        if(!runtimeVars.isEmpty()){
            content.put("set-runtime-vars", runtimeVars);
        }

        key.setContent(content);

    }


}
