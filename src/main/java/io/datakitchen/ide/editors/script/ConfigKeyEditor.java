package io.datakitchen.ide.editors.script;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTabbedPane;
import com.intellij.ui.table.JBTable;
import io.datakitchen.ide.editors.DocumentChangeEvent;
import io.datakitchen.ide.editors.DocumentChangeListener;
import io.datakitchen.ide.editors.DocumentEditor;
import io.datakitchen.ide.json.CustomJsonParser;
import io.datakitchen.ide.ui.*;
import io.datakitchen.ide.util.JsonUtil;
import io.datakitchen.ide.util.ObjectUtil;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

public class ConfigKeyEditor extends FormPanel implements DocumentEditor, Disposable {

    private final EventSupport<DocumentChangeListener> eventSupport = EventSupport.of(DocumentChangeListener.class);

    private final JComboBox mainScript = new ComboBox();
    private final JTextField manualMainScript = new JTextField();
    private final Editor parametersJson;
    private JTable environment = new JBTable();
    private final DefaultTableModel exportsModel = new DefaultTableModel(new String[]{"Variable name"},0);
    private final JTable exports = new JBTable(exportsModel);

    private final FieldListener listener = new FieldListener(this::documentChanged);
    private final Action addExportAction = new SimpleAction("Add", this::addExport);
    private final Action removeExportAction = new SimpleAction("Remove", this::removeExport);
    private final Action addEnvironment = new SimpleAction("Add",this::addEnvironment);
    private final Action removeEnvironment = new SimpleAction("Remove",this::removeEnvironment);

    private final Project project;
    private final VirtualFile dockerShareFolder;

    public ConfigKeyEditor(Project project, VirtualFile dockerShareFolder){
        super(new Dimension(300,28), new Dimension(200,28));
        this.project = project;
        this.dockerShareFolder = dockerShareFolder;

        mainScript.setModel(buildScriptsModel());

        addField("Main Script",mainScript,new Dimension(300, 28));
        addField(".. or manual script entry",manualMainScript,new Dimension(300, 28));

        environment.setModel(new DefaultTableModel(new String[]{"Variable","Value"},0));
        parametersJson = EditorUtil.createJsonEditor(project);
        JComponent editorComponent = parametersJson.getComponent();

        JTabbedPane tabs = new JBTabbedPane();
        tabs.addTab("Parameters", editorComponent);

        JPanel environmentPanel = new JPanel(new BorderLayout());
        environmentPanel.add(new JBScrollPane(environment, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED), BorderLayout.CENTER);
        JPanel envButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        envButtons.add(new JButton(addEnvironment));
        envButtons.add(new JButton(removeEnvironment));
        environmentPanel.add(envButtons,BorderLayout.SOUTH);

        tabs.addTab("Environment variables", environmentPanel);

        addField("Inputs",tabs,new Dimension(600,300));

        exports.setTableHeader(null);
        JScrollPane exportsScroll = new JBScrollPane(exports, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        JPanel exportsPanel = new JPanel(new BorderLayout());
        exportsPanel.add(exportsScroll,BorderLayout.CENTER);

        addField("Exports", exportsPanel,new Dimension(300,150));

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(new JButton(addExportAction));
        buttons.add(new JButton(removeExportAction));
        exportsPanel.add(buttons,BorderLayout.SOUTH);

        listener.listen(environment);
        listener.listen(exports);
        listener.listen(mainScript);
        listener.listen(manualMainScript);
        listener.listen(parametersJson);
    }

    @Override
    public void dispose() {
        EditorFactory.getInstance().releaseEditor(parametersJson);
    }

    private ComboBoxModel<String> buildScriptsModel() {

        List<String> items = Arrays.stream(dockerShareFolder.getChildren())
            .map(VirtualFile::getName)
            .filter((String name) -> !name.equals("config.json")).collect(Collectors.toList());

        return new DefaultComboBoxModel<>(items.toArray(String[]::new));
    }

    private void documentChanged() {
        updateActions();
        eventSupport.getProxy().documentChanged(new DocumentChangeEvent(this));
    }

    private void addExport(ActionEvent e){
        exportsModel.addRow(new Object[]{""});
        documentChanged();
    }

    private void removeExport(ActionEvent e){
        int index = exports.getSelectedRow();
        if (index != -1){
            exportsModel.removeRow(index);
        }
        documentChanged();
    }

    private void addEnvironment(ActionEvent event) {
        ((DefaultTableModel)environment.getModel()).addRow(new String[]{"",""});
        updateActions();
    }

    private void removeEnvironment(ActionEvent event) {
        ((DefaultTableModel)environment.getModel()).removeRow(environment.getSelectedRow());
        updateActions();
    }

    private void updateActions(){
        removeExportAction.setEnabled(exports.getSelectedRow() != -1);
        mainScript.setEnabled(StringUtils.isBlank(manualMainScript.getText()));
        removeEnvironment.setEnabled(environment.getSelectedRow() != -1);
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

    public void loadKey(ConfigKey key){
        listener.setEnabled(false);

        while (exportsModel.getRowCount() > 0){
            exportsModel.removeRow(0);
        }
        while (environment.getRowCount() > 0){
            ((DefaultTableModel)environment.getModel()).removeRow(0);
        }

        if (key != null){
            String scriptName = (String)key.getContent().get("script");
            if (((DefaultComboBoxModel)mainScript.getModel()).getIndexOf(scriptName) == -1){
                manualMainScript.setText(scriptName);
            } else{
                manualMainScript.setText("");;
                mainScript.setSelectedItem(scriptName);
            }
            ApplicationManager.getApplication().runWriteAction(()->
                parametersJson.getDocument().setText(JsonUtil.toJsonString(key.getContent().get("parameters")))
            );
            List<String> exports = ObjectUtil.cast(key.getContent().get("export"));

            Map<String, String> environment = ObjectUtil.cast(key.getContent().get("environment"));

            if (environment != null) {
                for (Map.Entry<String, String> entry : environment.entrySet()) {
                    ((DefaultTableModel) this.environment.getModel()).addRow(new String[]{entry.getKey(), entry.getValue()});
                }
            }

            for (String export : exports) {
                exportsModel.addRow(new Object[]{export});
            }
            this.environment.setEnabled(true);
            mainScript.setEnabled(true);
            manualMainScript.setEnabled(true);
            this.exports.setEnabled(true);
            parametersJson.getContentComponent().setEnabled(true);
        } else {
            mainScript.setSelectedItem(null);
            manualMainScript.setText("");
            ApplicationManager.getApplication().runWriteAction(()->
                parametersJson.getDocument().setText("")
            );
            environment.setEnabled(false);
            mainScript.setEnabled(false);
            manualMainScript.setEnabled(false);
            exports.setEnabled(false);
            parametersJson.getContentComponent().setEnabled(false);
        }
        listener.setEnabled(true);
    }

    public void saveKey(ConfigKey key){
        System.out.println("Save key");
        String script = manualMainScript.getText();
        if (StringUtils.isBlank(script)){
            script = (String)mainScript.getSelectedItem();
        }
        key.getContent().put("script",script);
        key.getContent().put("export",getExports());
        key.getContent().put("parameters", getParameters());
        key.getContent().put("environment", getEnvironment());

    }

    private Map<String,String> getEnvironment() {
        Map<String,String> env = new LinkedHashMap<>();
        for (int i=0;i<environment.getRowCount();i++){
            String name = (String)environment.getValueAt(i,0);
            String value = (String)environment.getValueAt(i,1);
            if (StringUtils.isNotBlank(name)) {
                env.put(name, value);
            }
        }
        return env;
    }

    private Map<String,Object> getParameters() {
        try {
            return CustomJsonParser.parse(parametersJson.getDocument().getText());
        }catch(Exception ex){
            return new LinkedHashMap<>();
        }
    }

    private List<String> getExports() {
        List<String> array = new ArrayList<>();

        for (int i=0;i<exportsModel.getRowCount();i++){
            String export = (String) exportsModel.getValueAt(i,0);
            if (StringUtils.isNotBlank(export)){
                array.add(export);
            }
        }

        return array;
    }
}
