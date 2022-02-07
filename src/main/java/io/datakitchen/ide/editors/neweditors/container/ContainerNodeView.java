package io.datakitchen.ide.editors.neweditors.container;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.JBUI;
import io.datakitchen.ide.dialogs.EnterFileNameDialog;
import io.datakitchen.ide.editors.neweditors.*;
import io.datakitchen.ide.editors.neweditors.palette.ComponentSource;
import io.datakitchen.ide.editors.neweditors.palette.ConnectorPalette;
import io.datakitchen.ide.model.*;
import io.datakitchen.ide.ui.*;
import io.datakitchen.ide.util.RecipeUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.TitledBorder;
import javax.swing.text.PlainDocument;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ContainerNodeView extends JPanel implements ConnectionListViewListener {

    private final JEditorPane description = new JEditorPane();
    private final ConnectionListView sources = new ConnectionListView();
    private final ConnectionListView sinks = new ConnectionListView();
    protected final FilesContainer inputFiles;
    protected final FilesContainer testInputFiles;
    private final VariablesContainer variableAssignments;
    private final ContainerView containerView;
    private final ContainerModel model;
    private RunRequestHandler runRequestHandler;

    public ContainerNodeView(ContainerModel model, ComponentSource componentSource){
        this.model = model;
        containerView = createContainerView();
        inputFiles = new FilesContainer(model, false);
        testInputFiles = new FilesContainer(model, true);
        variableAssignments = new VariablesContainer(model);
        JPanel centerPanel = new JPanel(new BorderLayout());

        sources.setEmptyText("Drag and drop here\n connectors to create\n source connections");
        sinks.setEmptyText("Drag and drop here\n connectors to create\n sink connections");
        sources.setKeyViewFactory(this::createSourceFileView);
        sinks.setKeyViewFactory(this::createSinkFileView);
        sources.setViewFactory(this::createSourceView);
        sinks.setViewFactory(this::createSinkView);
        sinks.addConnectionListViewListener(this);

        sources.setConnectionList(model.getDataSources());
        sources.setMinimumSize(new Dimension(200,200));
        sinks.setConnectionList(model.getDataSinks());
        sinks.setMinimumSize(new Dimension(200,200));
        sources.setBorder(
            new TitledBorder(JBUI.Borders.empty(1),"Sources", TitledBorder.RIGHT, TitledBorder.DEFAULT_POSITION)
        );
        sinks.setBorder(
            new TitledBorder(JBUI.Borders.empty(1),"Sinks")
        );
        containerView.setBorder(
            new TitledBorder(JBUI.Borders.empty(5),"Container")
        );
        JPanel leftPanel = new JPanel(new VerticalStackLayout());
        leftPanel.add(sources);
        leftPanel.add(inputFiles);
        leftPanel.add(testInputFiles);
        leftPanel.setBorder(new TitledBorder(LineBorder.right(),"Inputs", TitledBorder.RIGHT, TitledBorder.DEFAULT_POSITION));
        leftPanel.setMaximumSize(new Dimension(300,1000));

        JPanel rightPanel = new JPanel(new VerticalStackLayout());
        rightPanel.add(sinks);
        rightPanel.add(variableAssignments);
        rightPanel.setBorder(new TitledBorder(LineBorder.left(),"Outputs"));
        rightPanel.setMaximumSize(new Dimension(300,1000));

        JScrollPane scroll = new JBScrollPane(description, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setPreferredSize(new Dimension(100,100));
        JPanel descriptionArea = new JPanel(new BorderLayout());
        descriptionArea.add(scroll, BorderLayout.CENTER);
        descriptionArea.setBorder(
            new CompoundBorder(
                new TitledBorder(LineBorder.top(), "Description"),
                LineBorder.bottom()
            )
        );
        description.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                model.setDescription(description.getText());
            }
        });
        description.setText(model.getDescription());
        description.getDocument().putProperty(PlainDocument.tabSizeAttribute,4);


        JPanel containerArea = new JPanel(new BorderLayout());
        containerArea.add(containerView, BorderLayout.CENTER);
        containerArea.add(new Arrow(null), BorderLayout.WEST);
        containerArea.add(new Arrow(null), BorderLayout.EAST);
        containerArea.setPreferredSize(new Dimension(400,300));

        NodeTestsView tests = new NodeTestsView(model, this::onAddTestRequests);
        tests.setBorder(new TitledBorder(LineBorder.top(),"Node Tests"));

        centerPanel.add(containerArea, BorderLayout.CENTER);
        centerPanel.add(descriptionArea, BorderLayout.NORTH);
        centerPanel.add(leftPanel, BorderLayout.WEST);
        centerPanel.add(rightPanel, BorderLayout.EAST);
        centerPanel.add(tests, BorderLayout.SOUTH);
        leftPanel.setMinimumSize(new Dimension(200,200));
        rightPanel.setMinimumSize(new Dimension(200,200));

        setLayout(new BorderLayout());
        add(centerPanel, BorderLayout.CENTER);
        ConnectorPalette palette = new ConnectorPalette();
        palette.setComponentSource(componentSource);
        add(palette, BorderLayout.EAST);


    }

    private void onAddTestRequests() {
        TestEditorDialog dialog = new TestEditorDialog(model.getModule(), model.getNodeName());
        if (dialog.showAndGet()){
            model.addTest(dialog.createTest());
        }
    }

    public Project getProject(){
        return model.getProject();
    }

    public RunRequestHandler getRunRequestHandler() {
        return runRequestHandler;
    }

    public void setRunRequestHandler(RunRequestHandler runRequestHandler) {
        this.runRequestHandler = runRequestHandler;
        this.sources.setRunRequestHandler(runRequestHandler);
    }

    @NotNull
    protected ContainerView createContainerView() {
        return new DefaultContainerView(this);
    }

    private ConnectionView createSinkView(ConnectionListView connectionListView, Connection connection) {
        if (connection.getConnector().getConnectorType().getNature() == ConnectorNature.FILE){
            return new DataSinkFileConnectionView(connectionListView, connection, this::addOutputFileKey);
        }
        return new DataSinkSqlConnectionView(connectionListView, connection, this::addOutputSqlKey);
    }

    private ConnectionView createSourceView(ConnectionListView connectionListView, Connection connection) {

        ConnectorNature nature = connection.getConnector().getConnectorType().getNature();

        switch(nature){
            case FILE:
                return new DataSourceFileConnectionView(connectionListView, connection, this::addInputFile);
            case SQL:
                return new DataSourceSqlConnectionView(connectionListView, connection, this::addSqlFile);
            default:
                return new ConnectionView(connectionListView, connection);
        }

    }

    private KeyView createSinkFileView(Connection connection, Key key) {
        return new SinkFileView((DataSinkKey) key, this::getSinkFileActions);
    }

    private KeyView createSourceFileView(Connection connection, Key key) {
        SourceFileView sourceKeyView = new SourceFileView((DataSourceKey) key, this::getSourceFileActions);

        if (key instanceof DataSourceSqlKey) {
            sourceKeyView.setDoubleClickAction(new SimpleAction("", e -> this.openFile((DataSourceSqlKey) key)));
        }

        return sourceKeyView;
    }

    private Action[] getSourceFileActions(LabelWithActions requester) {
        SourceFileView fileView = (SourceFileView)requester;
        Key key = fileView.getKey();

        List<Action> actions = new ArrayList<>();
        if (key instanceof DataSourceSqlKey){
            DataSourceSqlKey sqlKey = (DataSourceSqlKey)key;
            actions.addAll(
                List.of(
                    new SimpleAction("Dump as CSV", e -> sqlKey.setDumpType(DumpType.CSV)),
                    new SimpleAction("Dump as Binary", e -> sqlKey.setDumpType(DumpType.BINARY)),
                    new SimpleAction("Dump as JSON", e -> sqlKey.setDumpType(DumpType.JSON)),
                    new SimpleAction("Details", e -> editDetails(sqlKey)),
                    new SimpleAction("Open SQL File", e -> openFile(sqlKey))
                )
            );
        }
        return actions.toArray(Action[]::new);
    }

    private Action[] getSinkFileActions(LabelWithActions requester) {
        SinkFileView fileView = (SinkFileView) requester;
        Key key = fileView.getKey();
        return new Action[]{
            new SimpleAction("Details", e -> editSinkDetails(key)),
        };
    }

    private void editSinkDetails(Key key) {
        if (key instanceof DataSinkSqlKey){
            DataSinkSqlKey sqlKey = (DataSinkSqlKey)key;
            SQLSInkKeyDetailsDialog dialog = new SQLSInkKeyDetailsDialog(sqlKey);
            if (dialog.showAndGet()){
                dialog.writeToKey(sqlKey);
            }
        }
    }

    private void openFile(DataSourceSqlKey key) {
        Module module = model.getModule();
        VirtualFile recipeFolder = RecipeUtil.recipeFolder(module);
        VirtualFile resourcesFolder = recipeFolder.findChild("resources");
        if (resourcesFolder == null){
            return;
        }
        VirtualFile file = resourcesFolder.findFileByRelativePath(key.getQueryFile());
        if (file == null){
            return;
        }
        FileEditorManager.getInstance(module.getProject()).openFile(file, true);
    }

    private void editDetails(DataSourceSqlKey key) {
        SQLSourceKeyDetailsDialog dialog = new SQLSourceKeyDetailsDialog( key);
        if (dialog.showAndGet()){
            dialog.writeToKey(key);
        }
    }

    public ContainerModel getModel() {
        return model;
    }

    @Override
    public void connectionViewAdded(ConnectionListViewEvent e) {
    }

    private void addInputFile(Connection connection) {
        FileSourceEditor editor = new FileSourceEditor();
        if (editor.showAndGet()){
            DataSourceFileKey key = new DataSourceFileKey(
                    (ConnectionImpl) connection,
                    makeKeyName(editor.getContainerFile()),
                    editor.getSourceFile(),
                    editor.getContainerFile()
            );

            ((ConnectionImpl)connection).addKey(key);
        }

    }

    private void addSqlFile(Connection connection) {
        EnterFileNameDialog dialog = new EnterFileNameDialog(model.getModule());
        if (dialog.showAndGet()){
            String fileName = dialog.getName();
            ApplicationManager.getApplication().runWriteAction(()->{
                try {
                    VirtualFile newFile = RecipeUtil
                            .recipeFolder(this.model.getModule())
                            .findChild("resources")
                            .createChildData(ContainerNodeView.this, fileName);
                    connection.addKeyFromFile(newFile);
                }catch (IOException ex){
                    ex.printStackTrace();
                }
            });
        }
    }

    private void addOutputSqlKey(Connection connection) {
        SqlSinkKeyEditor editor = new SqlSinkKeyEditor();
        if (editor.showAndGet()){
            DataSinkSqlKey key = new DataSinkSqlKey(
                    (ConnectionImpl) connection,
                    makeKeyName(editor.getFileName()),
                    editor.getFileName(),
                    editor.getTableName()
            );
            ((ConnectionImpl)connection).addKey(key);
        }
    }

    private void addOutputFileKey(Connection connection) {

        FileSinkEditor editor = new FileSinkEditor();
        if (editor.showAndGet()){
            DataSinkFileKey key = new DataSinkFileKey(
                (ConnectionImpl) connection,
                makeKeyName(editor.getSinkFile()),
                editor.getContainerFile(),
                editor.getSinkFile()
            );
            ((ConnectionImpl)connection).addKey(key);
        }
    }

    private String makeKeyName(String fileName){
        return fileName.replace(".","-").replace("/","-");
    }


}
