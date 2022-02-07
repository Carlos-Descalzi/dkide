package io.datakitchen.ide.editors.sql;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.VerticalFlowLayout;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ui.JBUI;
import io.datakitchen.ide.editors.DocumentChangeEvent;
import io.datakitchen.ide.editors.DocumentEditor;
import io.datakitchen.ide.editors.RecipeElementEditor;
import io.datakitchen.ide.ui.*;
import io.datakitchen.ide.util.JsonUtil;
import io.datakitchen.ide.util.ObjectUtil;
import io.datakitchen.ide.util.RecipeUtil;
import org.jdesktop.swingx.plaf.basic.core.BasicTransferable;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class SQLDataSinkEditor extends RecipeElementEditor implements ClipboardOwner {

    private ItemList<SqlKey> keysList;
    private SQLSinkKeyEditor keyEditor;
    private JPanel keysPanel;
    private JRadioButton enterConfiguration;
    private JRadioButton configFromVariable;
    private ComboBox<String> variable;
    private SqlKey currentKey;
    private Action copyAction;
    private Action pasteAction;

    private FieldListener listener;

    public SQLDataSinkEditor(Project project, VirtualFile file){
        super(project, file);
    }

    @Override
    protected Map<String, JComponent> getTabs() {

        listener = new FieldListener(this::documentChanged);

        copyAction = new SimpleAction(AllIcons.Actions.Copy, "Copy",this::copyKey);
        pasteAction = new SimpleAction(AllIcons.Actions.MenuPaste, "Paste",this::pasteKey);

        keysList = new ItemList<>(this::createKey, true);
        keysPanel = new JPanel();
        keyEditor = buildKeyEditor();
        Disposer.register(this, keyEditor);

        keysPanel.setLayout(new BorderLayout());
        keysPanel.setBorder(JBUI.Borders.empty(10));

        keysPanel.add(keysList,BorderLayout.WEST);
        keysPanel.add(keyEditor,BorderLayout.CENTER);

        keyEditor.addDocumentChangeListener(this::updateKey);


        JPanel configPanel = new JPanel();
        configPanel.setBorder(JBUI.Borders.empty(10));
        configPanel.setLayout(new VerticalFlowLayout(VerticalFlowLayout.TOP));
        enterConfiguration = new JRadioButton("Manual configuration");
        configFromVariable = new JRadioButton("Configuration from variable");
        ButtonGroup group = new ButtonGroup();
        group.add(enterConfiguration);
        group.add(configFromVariable);
        enterConfiguration.setSelected(true);

        JComponent configDetailsPanel = buildConfigurationPanel();
        if (configDetailsPanel instanceof DocumentEditor){
            listener.listen(((DocumentEditor) configDetailsPanel));
        }
        configDetailsPanel.setBorder(JBUI.Borders.empty(10));

        JPanel variableForm = buildEnterVariableForm();
        variableForm.setBorder(JBUI.Borders.empty(10));

        configPanel.add(enterConfiguration);
        configPanel.add(configDetailsPanel);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.setPreferredSize(new Dimension(500,30));
        JButton copyButton = new JButton(copyAction);
        copyButton.setPreferredSize(new Dimension(20,28));
        buttons.add(copyButton);
        JButton pasteButton = new JButton(pasteAction);
        pasteButton.setPreferredSize(new Dimension(20,28));
        buttons.add(pasteButton);
        JPanel buttonsContent = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonsContent.add(buttons);
        configPanel.add(buttonsContent);

        configPanel.add(configFromVariable);
        configPanel.add(variableForm);

        keysList.addListSelectionListener(this::selectKey);
        listener.listen(keysList);
        listener.listen(enterConfiguration);
        listener.listen(configFromVariable);
        listener.listen(keyEditor);

        variable.setModel(new DefaultComboBoxModel<>(RecipeUtil.getVariableNames(ModuleUtil.findModuleForFile(file, project)).toArray(String[]::new)));
        Map<String,JComponent> tabs = new LinkedHashMap<>();

        selectKey(null);
        tabs.put("Configuration", configPanel);
        tabs.put("Keys", keysPanel);
        return tabs;
    }

    private SqlKey createKey() {
        return new SqlKey("key-"+(keysList.getDataSize()+1));
    }

    private void documentChanged() {
        saveDocument();
        updateActions();
    }

    private void updateActions(){
        setConfigurationEnabled(enterConfiguration.isSelected());
        variable.setEnabled(configFromVariable.isSelected());
    }

    @NotNull
    private JPanel buildEnterVariableForm() {
        JPanel form = new JPanel(new FormLayout(5,5));

        JLabel l = new JLabel("Variable");
        l.setPreferredSize(new Dimension(100,28));
        form.add(l);
        variable = new ComboBox<>();
        l.setLabelFor(variable);
        variable.setPreferredSize(new Dimension(200,28));
        form.add(variable);
        form.setPreferredSize(new Dimension(500,400));

        return form;
    }

    private void updateKey(DocumentChangeEvent documentChangeEvent) {
        if (currentKey != null){
            keyEditor.saveKey(currentKey);
//            saveDocument();
        }
    }

    private void selectKey(ListSelectionEvent e) {
        if (currentKey != null){
            keyEditor.saveKey(currentKey);
        }
        currentKey = keysList.getSelected();
        if (currentKey != null) {
            keyEditor.loadKey(currentKey);
        } else {
            keyEditor.clear();
        }
        keyEditor.setEnabled(currentKey != null);
    }

    protected SQLSinkKeyEditor buildKeyEditor(){
        Module module = ModuleUtil.findModuleForFile(file, project);
        return new SQLSinkKeyEditor(module);
    }

    @Override
    protected void doLoadDocument(Map<String,Object> document) {

        String configRef = (String)document.get("config-ref");

        if (configRef != null){
            variable.setSelectedItem(configRef);
            configFromVariable.setSelected(true);
        } else {
            Map<String, Object> config = ObjectUtil.cast(document.get("config"));
            if (config == null){
                config = document;
            }
            loadConfiguration(config);
            enterConfiguration.setSelected(true);
        }

        Map<String, Object> keys = ObjectUtil.cast(document.get("keys"));
        loadKeys(keys);
    }

    @Override
    protected void doSaveDocument(Map<String,Object> document) {
        System.out.println("Saving data source");
        document.put("name", file.getName().replace(".json",""));
        document.put("type", getDsType());

        if (configFromVariable.isSelected()){
            document.put("config-ref", variable.getSelectedItem());
        } else {
            Map<String, Object> config = new LinkedHashMap<>();
            saveConfiguration(config);
            document.put("config",config);
        }

        Map<String, Object> keys = new LinkedHashMap<>();
        saveKeys(keys);
        document.put("keys",keys);
    }

    private void loadKeys(Map<String, Object> document) {
        List<SqlKey> keys = new ArrayList<>();
        if (document != null){
            keys.addAll(
                document
                        .entrySet()
                        .stream()
                        .map(SqlKey::fromEntry)
                        .collect(Collectors.toList())
            );
        }
        keysList.setData(keys);
        this.keyEditor.reloadResources();
        updateActions();
        if (keys.size() > 0){
            this.keysList.setSelectedIndex(0);
        } else {
            selectKey(null);
        }
    }

    private SqlKey buildKey(Map.Entry<String, Object> entry){
        return new SqlKey(entry.getKey(), ObjectUtil.cast (entry.getValue()));
    }

    private void saveKeys(Map<String, Object> document) {
        if (currentKey != null) {
            keyEditor.saveKey(currentKey);
        }
        for (SqlKey key: keysList.getData()){
            document.put(key.getName(),key.getContent());
        }
    }

    private void copyKey(ActionEvent e){
        Map<String, Object> config = new LinkedHashMap<>();
        saveConfiguration(config);
        String keyString = JsonUtil.toJsonString(config);
        Transferable transferable = new BasicTransferable(keyString, null);
        keysPanel.getToolkit().getSystemClipboard().setContents(transferable, this);
    }
    private void pasteKey(ActionEvent e){
        Transferable transferable = keysPanel.getToolkit().getSystemClipboard().getContents(this);
        if (transferable.isDataFlavorSupported(DataFlavor.stringFlavor)){
            try {
                String stringContent = (String) transferable.getTransferData(DataFlavor.stringFlavor);
                Map<String, Object> jsonData = JsonUtil.read(stringContent);
                loadConfiguration(jsonData);
            }catch (Exception ex){
                ex.printStackTrace();
            }
        }
    }

    @Override
    public void lostOwnership(Clipboard clipboard, Transferable contents) {

    }
    protected abstract void setConfigurationEnabled(boolean selected);

    @Override
    public @Nls(capitalization = Nls.Capitalization.Title) @NotNull String getName() {
        return "Data Source Editor";
    }

    protected abstract String getDsType();

    protected abstract JComponent buildConfigurationPanel();

    protected abstract void loadConfiguration(Map<String, Object> config);

    protected abstract void saveConfiguration(Map<String, Object> config);

}
