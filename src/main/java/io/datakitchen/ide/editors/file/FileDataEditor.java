package io.datakitchen.ide.editors.file;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.VerticalFlowLayout;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ui.JBUI;
import io.datakitchen.ide.editors.AbstractDataSourceEditor;
import io.datakitchen.ide.editors.DocumentChangeEvent;
import io.datakitchen.ide.editors.DocumentEditor;
import io.datakitchen.ide.editors.sql.SqlKey;
import io.datakitchen.ide.ui.*;
import io.datakitchen.ide.util.JsonUtil;
import io.datakitchen.ide.util.ObjectUtil;
import io.datakitchen.ide.util.RecipeUtil;
import org.jdesktop.swingx.plaf.basic.core.BasicTransferable;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
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

public abstract class FileDataEditor extends AbstractDataSourceEditor implements ClipboardOwner {

    private ItemList<FileKey> keysList;
    private FileKeyEditor keyEditor;
    private JPanel keysPanel;
    private JRadioButton enterConfiguration;
    private JRadioButton configFromVariable;
    private JComboBox<String> variable;
    private FileKey currentKey;

    public FileDataEditor(Project project, VirtualFile file){
        super(project, file);
    }

    @Override
    protected Map<String, JComponent> getTabs() {

        FieldListener listener = new FieldListener(this::documentChanged);

        Action copyAction = new SimpleAction(AllIcons.Actions.Copy, "Copy", this::copyKey);
        Action pasteAction = new SimpleAction(AllIcons.Actions.MenuPaste, "Paste", this::pasteKey);

        keysList = new ItemList<>(this::createFileKey, true);
        keysPanel = new JPanel(new BorderLayout());

        keysPanel.add(buildWildcardPanel(), BorderLayout.NORTH);

        keyEditor = buildKeyEditor();

        Border border = JBUI.Borders.empty(10);

        keysPanel.setBorder(border);
        keyEditor.setBorder(border);
        keysPanel.add(keysList,BorderLayout.WEST);
        keysPanel.add(keyEditor,BorderLayout.CENTER);

        keyEditor.addDocumentChangeListener(this::updateKey);


        JPanel configPanel = new JPanel();
        configPanel.setBorder(border);
        configPanel.setLayout(new VerticalFlowLayout(VerticalFlowLayout.TOP));
        enterConfiguration = new JRadioButton("Manual configuration");
        configFromVariable = new JRadioButton("Configuration from variable");
        ButtonGroup group = new ButtonGroup();
        group.add(enterConfiguration);
        group.add(configFromVariable);
        enterConfiguration.setSelected(true);

        JComponent configDetailsPanel = buildConfigurationPanel();
        configDetailsPanel.setBorder(border);
        if (configDetailsPanel instanceof DocumentEditor){
            listener.listen(((DocumentEditor) configDetailsPanel));
        }
        JPanel variableForm = buildEnterVariableForm();
        variableForm.setBorder(border);

        configPanel.add(enterConfiguration);
        configPanel.add(configDetailsPanel);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.setPreferredSize(new Dimension(400,30));
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
        enterConfiguration.addChangeListener((ChangeEvent e)-> updateActions());
        configFromVariable.addChangeListener((ChangeEvent e)-> updateActions());

        Map<String,JComponent> tabs = new LinkedHashMap<>();
        tabs.put("Configuration", configPanel);
        tabs.put("Keys", keysPanel);
        return tabs;
    }

    private FileKey createFileKey(){
        return new FileKey("key-"+(keysList.getDataSize()+1));
    }

    private void documentChanged() {
        saveDocument();
        updateActions();
    }

    @NotNull
    private JPanel buildEnterVariableForm() {
        FormPanel form = new FormPanel();

        variable = new ComboBox<>();
        form.addField("Variable",variable);

        return form;
    }


    private void updateKey(DocumentChangeEvent documentChangeEvent) {
        if (currentKey != null){
            keyEditor.saveKey(currentKey);
        }
    }

    private void selectKey(ListSelectionEvent e) {
        if (currentKey != null){
            keyEditor.saveKey(currentKey);
        }
        currentKey = keysList.getSelected();

        keyEditor.loadKey(currentKey);
    }

    protected FileKeyEditor buildKeyEditor(){
        return new FileKeyEditor(project);
    }

    @Override
    protected void doLoadDocument(Map<String,Object> document) {
        variable.setModel(new DefaultComboBoxModel<>(RecipeUtil.getVariableNames(ModuleUtil.findModuleForFile(file, project)).toArray(String[]::new)));

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

        loadWildcardSettings(document);

        Map<String, Object> keys = ObjectUtil.cast(document.get("keys"));
        loadKeys(keys);
    }


    @Override
    protected void doSaveDocument(Map<String,Object> document) {
        document.put("name",file.getName().replace(".json",""));
        document.put("type", getDsType());

        if (configFromVariable.isSelected()){
            document.put("config-ref", variable.getSelectedItem());
        } else {
            Map<String, Object> config = new LinkedHashMap<>();
            saveConfiguration(config);
            document.put("config",config);
        }

        saveWildcardSettings(document);

        Map<String, Object> keys = new LinkedHashMap<>();
        saveKeys(keys);
        document.put("keys",keys);
    }

    private void loadKeys(Map<String, Object> document) {
        List<FileKey> keys = new ArrayList<>();
        if (document != null){
            keys.addAll(
                    document
                            .entrySet()
                            .stream()
                            .map(FileKey::fromEntry)
                            .collect(Collectors.toList())
            );
        }
        keysList.setData(keys);
        updateActions();
        if (keys.size() > 0){
            keysList.setSelectedIndex(0);
        }
    }

    private SqlKey buildKey(Map.Entry<String, Object> entry){
        return new SqlKey(entry.getKey(), ObjectUtil.cast(entry.getValue()));
    }

    private void saveKeys(Map<String, Object> document) {
        if (currentKey != null) {
            keyEditor.saveKey(currentKey);
        }
        for (FileKey key: keysList.getData()){
            document.put(key.getName(),key.toJson());
        }
    }

    private void updateActions(){
        setConfigurationEnabled(enterConfiguration.isSelected());
        variable.setEnabled(configFromVariable.isSelected());
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
        return "Data Editor";
    }

    protected abstract void saveWildcardSettings(Map<String, Object> document);

    protected abstract void loadWildcardSettings(Map<String, Object> document);

    protected abstract String getDsType();

    protected abstract JComponent buildConfigurationPanel();

    protected abstract JComponent buildWildcardPanel();

    protected abstract void loadConfiguration(Map<String, Object> config);

    protected abstract void saveConfiguration(Map<String, Object> config);

}
