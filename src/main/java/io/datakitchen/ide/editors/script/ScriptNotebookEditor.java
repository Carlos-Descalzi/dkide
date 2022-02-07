package io.datakitchen.ide.editors.script;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.newvfs.BulkFileListener;
import com.intellij.openapi.vfs.newvfs.events.VFileEvent;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.JBTable;
import com.intellij.util.messages.MessageBusConnection;
import com.intellij.util.ui.JBUI;
import io.datakitchen.ide.editors.AbstractNodeEditor;
import io.datakitchen.ide.editors.DsInfo;
import io.datakitchen.ide.json.CustomJsonParser;
import io.datakitchen.ide.ui.*;
import io.datakitchen.ide.util.JsonUtil;
import io.datakitchen.ide.util.ObjectUtil;
import net.minidev.json.parser.ParseException;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ScriptNotebookEditor extends AbstractNodeEditor {
    private Map<String,DsInfo> dataSources;
    private Map<String,DsInfo> dataSinks;

    private JTextField maxRam;
    private JTextField maxStorage;
    private EntryField registry;
    private JCheckBox deleteOnFinish;
    private JTextArea jinjaIgnore;
    private ItemList<ConfigKey> configKeys;
    private ConfigKeyEditor configKeyEditor;

    private ComboBox<DsInfo> dataSource;
    private ComboBox<DsInfo> dataSink;
    private JTable inputFiles;
    private JTable outputFiles;
    private JTable assignments;
    private JTable aptDependencies;
    private JTable pipDependencies;
    private InputFilesModel inputModel;
    private OutputFilesModel outputModel;
    private AssignmentsModel assignmentsModel;
    private DefaultTableModel aptDependenciesModel;
    private DefaultTableModel pipDependenciesModel;

    private Action addInputAction;
    private Action removeInputAction;
    private Action addOutputAction;
    private Action removeOutputAction;
    private Action addAptDepdencency;
    private Action removeAptDependency;
    private Action addPipDepdencency;
    private Action removePipDependency;
    private Action addAssignmentAction;
    private Action removeAssignmentAction;

    private MessageBusConnection connection;

    private FieldListener listener;

    private ConfigKey currentConfigKey;

    @Override
    protected Map<String, JComponent> getTabs() {
        JPanel filesPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JPanel dependenciesPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JPanel scriptsPanel = buildMainScriptPanel();
        JPanel settingsPanel = buildSettingsPanel();

        JPanel filesPanelContent = new JPanel(new GridBagLayout());
        filesPanel.add(filesPanelContent);

        int i=0;
        for (JComponent c: new JComponent[]{buildInputFilesPanel(), buildOutputFilesPanel(), buildAssignmentsPanel()}){
            c.setPreferredSize(new Dimension(500,150));
            filesPanelContent.add(c, new GridBagConstraints(0,i++,1,1,1,1,GridBagConstraints.NORTHWEST,GridBagConstraints.NONE, new Insets(0,0,0,0),10,10));
        }

        JPanel dependenciesPanelContent = new JPanel(new GridBagLayout());
        dependenciesPanel.add(dependenciesPanelContent);
        i=0;
        for (JComponent c: new JComponent[]{buildAptDependenciesPanel(), buildPipDependenciesPanel()}){
            c.setPreferredSize(new Dimension(500,150));
            dependenciesPanelContent.add(c, new GridBagConstraints(0,i++,1,1,1,1,GridBagConstraints.NORTHWEST,GridBagConstraints.NONE, new Insets(0,0,0,0),10,10));
        }

        dataSources = new LinkedHashMap<>();
        dataSinks = new LinkedHashMap<>();

        updateActions();
        reloadModels();

        connection = this.project.getMessageBus().connect();
        connection.subscribe(VirtualFileManager.VFS_CHANGES, new BulkFileListener() {
            @Override
            public void after(@NotNull List<? extends @NotNull VFileEvent> events) {
                checkFileUpdates(events);
            }
        });

        Map<String,JComponent> tabs = new LinkedHashMap<>();
        tabs.put("Keys", scriptsPanel);
        tabs.put("Settings", settingsPanel);
        tabs.put("Files",filesPanel);
        tabs.put("Dependencies",dependenciesPanel);

        listener = new FieldListener(this::documentChanged);
        listener.listen(maxRam);
        listener.listen(maxStorage);
        listener.listen(deleteOnFinish);
        listener.listen(jinjaIgnore);
        listener.listen(configKeyEditor);
        listener.listen(inputFiles);
        listener.listen(outputFiles);
        listener.listen(assignments);
        listener.listen(pipDependencies);
        listener.listen(aptDependencies);

        return tabs;
    }

    private void checkFileUpdates(List<? extends @NotNull VFileEvent> events){
        List<String> paths = new ArrayList<>();
        for (String folderName:new String[]{"data_sources","data_sinks","docker-share"}){
            VirtualFile folder = file.getParent().findChild(folderName);
            if (folder != null){
                paths.add(folder.getPath());
            }
        }

        for (VFileEvent event:events){
            for (String path:paths){
                if (event.getPath().contains(path)){
                    reloadModels();
                    updateActions();
                    break;
                }
            }
        }

    }

    @Override
    public void dispose() {
        connection.disconnect();
    }

    public ScriptNotebookEditor(Project project, VirtualFile file){
        super(project, file);
    }

    private JPanel buildSettingsPanel() {
        FormPanel panel = new FormPanel(new Dimension(300,28), new Dimension(200,28));

        maxRam = new RegExValidatedField(RegExValidatedField.NUMBER);
        maxStorage = new RegExValidatedField(RegExValidatedField.NUMBER);
        registry = new EntryField(ModuleUtil.findModuleForFile(file,project));
        deleteOnFinish = new JCheckBox();
        jinjaIgnore = new JTextArea();

        panel.addField("Max. RAM (MB)",maxRam);
        panel.addField("Max. Storage (MB)",maxStorage);
        panel.addField("Alternative registry",registry);
        panel.addField("Delete container on finish", deleteOnFinish);

        panel.addField("Ignore jinja on files matching",
            new JBScrollPane(jinjaIgnore, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED),
            new Dimension(300,84)
        );
        return panel;
    }

    private JPanel buildMainScriptPanel(){
        JPanel configPanel = new JPanel(new BorderLayout());

        configKeys = new ItemList<>(this::createConfigKey, true);
        configPanel.add(configKeys,BorderLayout.WEST);
        configKeyEditor = new ConfigKeyEditor(project,getDockershareFolder());
        Disposer.register(this, configKeyEditor);
        configKeyEditor.setBorder(JBUI.Borders.empty(10));
        configPanel.add(configKeyEditor,BorderLayout.CENTER);
        configPanel.setBorder(JBUI.Borders.empty(10));
        configKeys.addListSelectionListener(this::onKeySelected);

        return configPanel;
    }

    private ConfigKey createConfigKey() {
        return new ConfigKey("key-"+(configKeys.getDataSize()+1));
    }

    private void onKeySelected(ListSelectionEvent listSelectionEvent) {
        if (this.currentConfigKey != null){
            configKeyEditor.saveKey(currentConfigKey);
        }
        currentConfigKey = (ConfigKey) configKeys.getSelected();
        configKeyEditor.loadKey(currentConfigKey);
    }

    private JPanel buildOutputFilesPanel() {
        dataSink = new ComboBox<>();
        outputFiles = new JBTable();
        outputModel = new OutputFilesModel();
        addOutputAction = new SimpleAction("Add", this::addOutputFile);
        removeOutputAction = new SimpleAction("Remove", this::removeOutputFile);
        outputFiles.getSelectionModel().addListSelectionListener((ListSelectionEvent e)->{updateActions();});

        outputFiles.setAutoCreateColumnsFromModel(false);
        outputFiles.setModel(outputModel);
        TableColumn tc = new TableColumn(0,100,new DefaultTableCellRenderer(),new DefaultCellEditor(new JTextField()));
        tc.setHeaderValue("Container path");
        outputFiles.getColumnModel().addColumn(tc);
        tc = new TableColumn(1,100,new DefaultTableCellRenderer(),new DefaultCellEditor(dataSink));
        tc.setHeaderValue("Data sink");
        outputFiles.getColumnModel().addColumn(tc);
        tc = new TableColumn(2,100,new DefaultTableCellRenderer(),new KeyEditor(1));
        tc.setHeaderValue("Key");
        outputFiles.getColumnModel().addColumn(tc);
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new CompoundBorder(new TitledBorder("Output files"), JBUI.Borders.empty(10)));
        panel.add(new JBScrollPane(outputFiles,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JBScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED), BorderLayout.CENTER);
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panel.add(buttons,BorderLayout.SOUTH);
        buttons.add(new JButton(addOutputAction));
        buttons.add(new JButton(removeOutputAction));
        return panel;
    }

    private JPanel buildInputFilesPanel() {
        dataSource = new ComboBox<>();
        inputFiles = new JBTable();
        inputModel = new InputFilesModel();
        addInputAction = new SimpleAction("Add", this::addInputFile);
        removeInputAction = new SimpleAction("Remove", this::removeInputFile);
        inputFiles.getSelectionModel().addListSelectionListener((ListSelectionEvent e)->{updateActions();});

        inputFiles.setAutoCreateColumnsFromModel(false);
        inputFiles.setModel(inputModel);
        TableColumn tc = new TableColumn(0,100,new DefaultTableCellRenderer(),new DefaultCellEditor(dataSource));
        tc.setHeaderValue("Data source");
        inputFiles.getColumnModel().addColumn(tc);
        tc = new TableColumn(1,100,new DefaultTableCellRenderer(),new KeyEditor(0));
        tc.setHeaderValue("Key");
        inputFiles.getColumnModel().addColumn(tc);
        tc = new TableColumn(2,100,new DefaultTableCellRenderer(),new DefaultCellEditor(new JTextField()));
        tc.setHeaderValue("Container path");
        inputFiles.getColumnModel().addColumn(tc);
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new TitledBorder("Input files"));
        panel.add(new JBScrollPane(inputFiles, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JBScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED), BorderLayout.CENTER);
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(new JButton(addInputAction));
        buttons.add(new JButton(removeInputAction));
        panel.add(buttons, BorderLayout.SOUTH);
        return panel;
    }
    private JPanel buildAssignmentsPanel(){
        assignmentsModel = new AssignmentsModel();
        assignments = new JBTable(assignmentsModel);
        addAssignmentAction = new SimpleAction("Add", this::addAssignment);
        removeAssignmentAction = new SimpleAction("Remove", this::removeAssignment);
        assignments.getSelectionModel().addListSelectionListener((ListSelectionEvent e)->{updateActions();});

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new TitledBorder("Variable assignments"));
        panel.add(new JBScrollPane(assignments, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JBScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED), BorderLayout.CENTER);
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(new JButton(addAssignmentAction));
        buttons.add(new JButton(removeAssignmentAction));
        panel.add(buttons, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildAptDependenciesPanel(){
        aptDependenciesModel = new DefaultTableModel(new Object[]{"Dependency"},0);
        aptDependencies = new JBTable(aptDependenciesModel);
        addAptDepdencency = new SimpleAction("Add", this::addApt);
        removeAptDependency = new SimpleAction("Remove", this::removeApt);
        aptDependencies.getSelectionModel().addListSelectionListener((ListSelectionEvent e)->{updateActions();});

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new TitledBorder("APT packages"));
        panel.add(new JBScrollPane(aptDependencies,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JBScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED), BorderLayout.CENTER);
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panel.add(buttons,BorderLayout.SOUTH);
        buttons.add(new JButton(addAptDepdencency));
        buttons.add(new JButton(removeAptDependency));
        return panel;
    }

    private JPanel buildPipDependenciesPanel(){
        pipDependenciesModel = new DefaultTableModel(new Object[]{"Dependency","Version"},0);
        pipDependencies = new JBTable(pipDependenciesModel);
        addPipDepdencency = new SimpleAction("Add", this::addPip);
        removePipDependency = new SimpleAction("Remove", this::removePip);
        pipDependencies.getSelectionModel().addListSelectionListener(e -> updateActions());

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new CompoundBorder(new TitledBorder("PIP packages"), JBUI.Borders.empty(10)));
        panel.add(new JBScrollPane(pipDependencies,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JBScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED), BorderLayout.CENTER);
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panel.add(buttons,BorderLayout.SOUTH);
        buttons.add(new JButton(addPipDepdencency));
        buttons.add(new JButton(removePipDependency));
        return panel;
    }

    private void documentChanged() {
        System.out.println("Saving document");
        saveDocument();
    }

    private VirtualFile getDockershareFolder(){
        return file.getParent().findChild("docker-share");
    }

    private VirtualFile getConfigJsonFile(){
        return getDockershareFolder().findChild("config.json");
    }

    private void reloadModels(){
        dataSources.clear();
        dataSinks.clear();
        List<DsInfo> sources = DsInfo.loadDsItems(this.file.getParent(),"data_sources", true);
        for (DsInfo item:sources){
            dataSources.put(item.getName(),item);
        }
        dataSource.setModel(new DefaultComboBoxModel<>(sources.toArray(DsInfo[]::new)));
        List<DsInfo> sinks = DsInfo.loadDsItems(this.file.getParent(),"data_sinks",true);
        for (DsInfo item:sinks){
            dataSinks.put(item.getName(),item);
        }
        dataSink.setModel(new DefaultComboBoxModel<>(sinks.toArray(DsInfo[]::new)));
    }

    private void addInputFile(ActionEvent actionEvent) {
        inputModel.addFile();
        updateActions();
    }

    private void removeInputFile(ActionEvent actionEvent) {
        int index = inputFiles.getSelectedRow();
        if (index != -1){
            inputModel.removeFile(index);
        }
        updateActions();
    }

    private void addOutputFile(ActionEvent actionEvent) {
        outputModel.addFile();
        updateActions();

    }

    private void removeOutputFile(ActionEvent actionEvent) {
        int index = outputFiles.getSelectedRow();
        if (index != -1){
            outputModel.removeFile(index);
        }
        updateActions();
    }

    private void addApt(ActionEvent actionEvent) {
        aptDependenciesModel.addRow(new Object[]{""});
        updateActions();
    }

    private void removeApt(ActionEvent actionEvent) {
        int index = aptDependencies.getSelectedRow();
        if (index != -1){
            aptDependenciesModel.removeRow(index);
        }
        updateActions();
    }
    private void addPip(ActionEvent actionEvent) {
        pipDependenciesModel.addRow(new Object[]{"",""});
        updateActions();
    }

    private void removePip(ActionEvent actionEvent) {
        int index = pipDependencies.getSelectedRow();
        if (index != -1){
            pipDependenciesModel.removeRow(index);
        }
        updateActions();
    }
    private void addAssignment(ActionEvent e){
        assignmentsModel.add(new VariableAssignment());
    }
    private void removeAssignment(ActionEvent e){
        int index = assignments.getSelectedRow();
        if (index != -1){
            assignmentsModel.remove(index);
        }
        updateActions();
    }

    protected void enableEvents(){

        listener.setEnabled(true);
    }
    protected void disableEvents(){
        listener.setEnabled(false);
    }

    private void updateActions(){
        boolean sourcesAvailable = dataSource.getModel().getSize() > 0;
        boolean sinksAvailable = dataSink.getModel().getSize() > 0;
        addInputAction.setEnabled(sourcesAvailable);
        removeInputAction.setEnabled(sourcesAvailable && inputFiles.getSelectedRow() != -1);
        addOutputAction.setEnabled(sinksAvailable);
        removeOutputAction.setEnabled(sinksAvailable && outputFiles.getSelectedRow() != -1);
        removeAptDependency.setEnabled(aptDependencies.getSelectedRow() != -1);
        removePipDependency.setEnabled(pipDependencies.getSelectedRow() != -1);
        removeAssignmentAction.setEnabled(assignments.getSelectedRow() != -1);
    }

    @Override
    public @Nls(capitalization = Nls.Capitalization.Title) @NotNull String getName() {
        return "Script Node Notebook";
    }

    @Override
    protected void doLoadDocument(Map<String,Object> document) throws ParseException{
        reloadModels();
        loadNotebook(document);
        loadConfigJson();
        updateActions();
        if (this.configKeys.getDataSize() > 0){
            this.configKeys.setSelectedIndex(0);
        }
    }

    private void loadNotebook(Map<String,Object> notebook){
        maxRam.setText(String.valueOf(notebook.getOrDefault("max-ram","")));
        maxStorage.setText(String.valueOf(notebook.getOrDefault("max-storage","")));
        deleteOnFinish.setSelected((Boolean)notebook.getOrDefault("delete-container-when-complete", true));

        List<String> jinjaIgnorePatterns = ObjectUtil.cast(notebook.get("jinja-ignore-patterns"));
        if (jinjaIgnorePatterns != null){
            jinjaIgnore.setText(String.join("\n",jinjaIgnorePatterns));
        }

        this.registry.setText((String)notebook.getOrDefault("dockerhub-url",""));
        try {
            List<Map<String,Object>> inputFiles = ObjectUtil.cast (notebook.get("container-input-file-keys"));

            if (inputFiles != null){
                List<FileReference> files = new ArrayList<>();
                for (Map<String, Object> inputFile : inputFiles) {
                    files.add(fileRefFromJson(inputFile, dataSources));
                }
                inputModel.setFileReferences(files);
            }
            List<Map<String,Object>> outputFiles = ObjectUtil.cast(notebook.get("container-output-file-keys"));

            if (outputFiles != null){
                List<FileReference> files = new ArrayList<>();
                for (Map<String, Object> outputFile : outputFiles) {
                    files.add(fileRefFromJson(outputFile, dataSinks));
                }
                outputModel.setFileReferences(files);
            }
            List<Map<String,Object>> assignVariables = ObjectUtil.cast(notebook.get("assign-variables"));

            if (assignVariables != null){
                List<VariableAssignment> assignments = new ArrayList<>();
                for (Map<String, Object> obj : assignVariables) {
                    assignments.add(VariableAssignment.fromJson(obj));

                }
                assignmentsModel.setVariableAssignments(assignments);
            }

        }catch(Exception ex){
            ex.printStackTrace();
        }
    }

    private void loadConfigJson() throws ParseException {
        try {
            Map<String,Object> configJson = CustomJsonParser.parse(getConfigJsonFile());

            List<String> aptDependencies = ObjectUtil.cast(configJson.get("apt-dependencies"));

            while (aptDependenciesModel.getRowCount() > 0) {
                aptDependenciesModel.removeRow(0);
            }
            for (String aptDependency : aptDependencies) {
                aptDependenciesModel.addRow(new Object[]{aptDependency});
            }

            List<String> pipDependencies = ObjectUtil.cast(configJson.get("dependencies"));

            while (pipDependenciesModel.getRowCount() > 0) {
                pipDependenciesModel.removeRow(0);
            }
            for (String pipDependency : pipDependencies) {
                String[] row;
                if (pipDependency.contains("==")) {
                    row = pipDependency.split("==");
                } else {
                    row = new String[]{pipDependency, ""};
                }
                pipDependenciesModel.addRow(row);
            }

            Map<String,Object> keys = ObjectUtil.cast(configJson.get("keys"));

            if (keys != null) {
                configKeys.setData(keys.entrySet().stream().map(ConfigKey::fromEntry).collect(Collectors.toList()));
            }

        }catch (IOException | io.datakitchen.ide.json.ParseException ex){
            ex.printStackTrace();
        }
    }

    private FileReference fileRefFromJson(Map<String,Object> obj, Map<String,DsInfo> source){
        FileReference fileReference = new FileReference();
        String[] key = ((String)obj.get("key")).split("\\.");
        fileReference.setSourceSink(source.get(key[0]));
        fileReference.setKey(key[1]);
        fileReference.setFileName((String)obj.get("filename"));
        return fileReference;
    }

    @Override
    protected void doSaveDocument(Map<String,Object> document) {
        updateNotebook(document);
        updateConfigJson();
    }

    private void updateNotebook(Map<String,Object> notebook){
        notebook.put("image-repo","{{gpcConfig.image_repo}}");
        notebook.put("image-tag","{{gpcConfig.image_tag}}");
        notebook.put("dockerhub-namespace","{{gpcConfig.namespace}}");
        notebook.put("dockerhub-username","{{gpcConfig.username}}");
        notebook.put("dockerhub-password","{{gpcConfig.password}}");
        notebook.put("delete-container-when-complete", deleteOnFinish.isSelected());

        String[] jinjaIgnoredPatterns = jinjaIgnore.getText().split("\n");

        if (jinjaIgnoredPatterns.length == 0){
            notebook.remove("jinja-ignore-patterns");
        } else {
            notebook.put("jinja-ignore-patterns",List.of(jinjaIgnoredPatterns));
        }

        String maxRam = this.maxRam.getText();
        if (StringUtils.isNotBlank(maxRam)){
            notebook.put("max-ram",Integer.parseInt(maxRam));
        } else {
            notebook.remove("max-ram");
        }
        String maxStorage = this.maxStorage.getText();
        if (StringUtils.isNotBlank(maxStorage)){
            notebook.put("max-storage",Integer.parseInt(maxStorage));
        } else {
            notebook.remove("max-storage");
        }
        String registry = this.registry.getText();
        if (StringUtils.isNotBlank(registry)){
            notebook.put("dockerhub-url", registry);
        } else {
            notebook.remove("dockerhub-url");
        }


        List<Map<String,Object>> inputFiles = new ArrayList<>();
        for (FileReference fileReference:inputModel.getFileReferences()){
            if (fileReference.isValid()){
                Map<String,Object> entry = new LinkedHashMap<>();
                entry.put("filename",fileReference.getFileName());
                entry.put("key",fileReference.getSourceSink().getName()+"."+fileReference.getKey());
                inputFiles.add(entry);
            }
        }
        notebook.put("container-input-file-keys", inputFiles);

        List<Map<String,Object>> outputFiles = new ArrayList<>();
        for (FileReference fileReference:outputModel.getFileReferences()){
            if (fileReference.isValid()){
                Map<String,Object> entry = new LinkedHashMap<>();
                entry.put("filename",fileReference.getFileName());
                entry.put("key",fileReference.getSourceSink().getName()+"."+fileReference.getKey());
                outputFiles.add(entry);
            }
        }

        notebook.put("container-output-file-keys", outputFiles);

        List<Map<String,Object>> assignVariables = new ArrayList<>();

        for (VariableAssignment assignment: assignmentsModel.getVariableAssignments()){
            if (assignment.isValid()){
                Map<String,Object> entry = new LinkedHashMap<>();
                assignVariables.add(assignment.toJson());
            }
        }

        notebook.put("assign-variables",assignVariables);

    }

    private void updateConfigJson(){

        System.out.println("Save config json");
        if (currentConfigKey != null){
            System.out.println("Save config key");
            configKeyEditor.saveKey(currentConfigKey);
        }

        Map<String,Object> configJson = new LinkedHashMap<>();
        configJson.put("apt-dependencies", getAptDependenciesJsonArray());
        configJson.put("dependencies", getPipDependenciesJsonArray());

        Map<String,Object> keysJson = new LinkedHashMap<>();

        for (ConfigKey configKey:this.configKeys.getData()){
            keysJson.put(configKey.getName(), configKey.getContent());
        }

        configJson.put("keys",keysJson);

        ApplicationManager.getApplication().runWriteAction(()->{
            try {
                VirtualFile configJsonFile = getDockershareFolder().findChild("config.json");
                if (configJsonFile == null) {
                    configJsonFile = getDockershareFolder().createChildData(this, "config.json");
                }
                JsonUtil.write(configJson, configJsonFile);
            }catch(Exception ex){
                ex.printStackTrace();
            }
        });
    }

    private List<String> getPipDependenciesJsonArray() {
        List<String> array = new ArrayList<>();

        for (int i=0;i<pipDependenciesModel.getRowCount();i++){
            String pipPackage = (String)pipDependenciesModel.getValueAt(i,0);
            String pipVersion = (String)pipDependenciesModel.getValueAt(i,1);
            if (StringUtils.isNotBlank(pipPackage)) {
                array.add(pipPackage + (StringUtils.isNotBlank(pipVersion) ? "==" + pipVersion : ""));
            }
        }

        return array;
    }

    private List<String> getAptDependenciesJsonArray() {
        List<String> array = new ArrayList<>();

        for (int i=0;i<aptDependenciesModel.getRowCount();i++){
            array.add((String)aptDependenciesModel.getValueAt(i,0));
        }

        return array;
    }


}
