package io.datakitchen.ide.editors;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.newvfs.BulkFileListener;
import com.intellij.openapi.vfs.newvfs.events.VFileEvent;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.JBTable;
import com.intellij.util.messages.MessageBusConnection;
import com.intellij.util.ui.JBUI;
import io.datakitchen.ide.editors.script.*;
import io.datakitchen.ide.ui.SimpleAction;
import io.datakitchen.ide.util.ObjectUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class BaseContainerNotebookEditor extends AbstractNodeEditor {
    private Map<String,DsInfo> dataSources;
    private Map<String,DsInfo> dataSinks;

    private JComboBox<DsInfo> dataSource;
    private JComboBox<DsInfo> dataSink;
    private JTable inputFiles;
    private JTable outputFiles;
    private JTable assignments;
    private InputFilesModel inputModel;
    private OutputFilesModel outputModel;
    private AssignmentsModel assignmentsModel;

    private Action addInputAction;
    private Action removeInputAction;
    private Action addOutputAction;
    private Action removeOutputAction;
    private Action addAssignmentAction;
    private Action removeAssignmentAction;

    private MessageBusConnection connection;

    @Override
    protected Map<String, JComponent> getTabs() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        JPanel filesPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JPanel dependenciesPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        mainPanel.add(buildMainPanel(),BorderLayout.NORTH);

        JPanel filesPanelContent = new JPanel(new GridBagLayout());
        filesPanel.add(filesPanelContent);

        int i=0;
        for (JComponent c: new JComponent[]{buildInputFilesPanel(), buildOutputFilesPanel(), buildAssignmentsPanel()}){
            c.setPreferredSize(new Dimension(500,150));
            filesPanelContent.add(c,
                    new GridBagConstraints(0,i++,1,1,1,1,
                            GridBagConstraints.NORTHWEST,GridBagConstraints.NONE, JBUI.emptyInsets(),10,10
                    )
            );
        }

        JPanel dependenciesPanelContent = new JPanel(new GridBagLayout());
        dependenciesPanel.add(dependenciesPanelContent);

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
        tabs.put("Main settings", mainPanel);
        tabs.put("Files",filesPanel);

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

    public BaseContainerNotebookEditor(Project project, VirtualFile file){
        super(project, file);
    }

    protected abstract JPanel buildMainPanel();

    private JPanel buildOutputFilesPanel() {
        dataSink = new ComboBox<>();
        outputFiles = new JBTable();
        outputModel = new OutputFilesModel();
        addOutputAction = new SimpleAction("Add", this::addOutputFile);
        removeOutputAction = new SimpleAction("Remove", this::removeOutputFile);
        outputFiles.getSelectionModel().addListSelectionListener(e -> updateActions());

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
        inputFiles.getSelectionModel().addListSelectionListener(e-> updateActions());

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
        assignments.getSelectionModel().addListSelectionListener(e->updateActions());

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new TitledBorder("Variable assignments"));
        panel.add(new JBScrollPane(assignments, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JBScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED), BorderLayout.CENTER);
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(new JButton(addAssignmentAction));
        buttons.add(new JButton(removeAssignmentAction));
        panel.add(buttons, BorderLayout.SOUTH);
        return panel;
    }

    protected void reloadModels(){
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

    protected void updateActions(){
        boolean sourcesAvailable = dataSource.getModel().getSize() > 0;
        boolean sinksAvailable = dataSink.getModel().getSize() > 0;
        addInputAction.setEnabled(sourcesAvailable);
        removeInputAction.setEnabled(sourcesAvailable && inputFiles.getSelectedRow() != -1);
        addOutputAction.setEnabled(sinksAvailable);
        removeOutputAction.setEnabled(sinksAvailable && outputFiles.getSelectedRow() != -1);
        removeAssignmentAction.setEnabled(assignments.getSelectedRow() != -1);
        addAssignmentAction.setEnabled(true);
    }

    @Override
    public @Nls(capitalization = Nls.Capitalization.Title) @NotNull String getName() {
        return "Script Node Notebook";
    }

    @Override
    protected void doLoadDocument(Map<String,Object> document) {
        reloadModels();
        loadNotebook(document);
        updateActions();
    }

    protected void loadNotebook(Map<String,Object> notebook){
        try {
            List<Map<String,Object>> inputFiles = ObjectUtil.cast(notebook.get("container-input-file-keys"));

            if (inputFiles != null){
                List<FileReference> files = inputFiles
                        .stream()
                        .map(obj -> fileRefFromJson(obj, dataSources))
                        .collect(Collectors.toList());
                inputModel.setFileReferences(files);
            }
            List<Map<String,Object>> outputFiles = ObjectUtil.cast(notebook.get("container-output-file-keys"));

            if (outputFiles != null){
                List<FileReference> files = outputFiles
                        .stream()
                        .map(obj -> fileRefFromJson(obj, dataSinks))
                        .collect(Collectors.toList());
                outputModel.setFileReferences(files);
            }
            List<Map<String,Object>> assignVariables = ObjectUtil.cast(notebook.get("assign-variables"));

            if (assignVariables != null){
                List<VariableAssignment> assignments = assignVariables
                        .stream()
                        .map(VariableAssignment::fromJson)
                        .collect(Collectors.toList());
                assignmentsModel.setVariableAssignments(assignments);
            }

        }catch(Exception ex){
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
    }

    protected void updateNotebook(Map<String,Object> notebook){

        List<Map<String,Object>> inputFiles = inputModel
                .getFileReferences()
                .stream()
                .filter(FileReference::isValid)
                .map(FileReference::toJson)
                .collect(Collectors.toList());

        notebook.put("container-input-file-keys", inputFiles);

        List<Map<String,Object>> outputFiles = outputModel
                .getFileReferences()
                .stream()
                .filter(FileReference::isValid)
                .map(FileReference::toJson)
                .collect(Collectors.toList());

        notebook.put("container-output-file-keys", outputFiles);

        List<Map<String,Object>> assignVariables = assignmentsModel
                .getVariableAssignments()
                .stream()
                .filter(VariableAssignment::isValid)
                .map(VariableAssignment::toJson)
                .collect(Collectors.toList());

        notebook.put("assign-variables",assignVariables);

    }


}
