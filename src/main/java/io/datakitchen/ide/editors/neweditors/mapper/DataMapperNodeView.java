package io.datakitchen.ide.editors.neweditors.mapper;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.components.JBScrollPane;
import io.datakitchen.ide.dialogs.EnterFileNameDialog;
import io.datakitchen.ide.editors.neweditors.*;
import io.datakitchen.ide.editors.neweditors.palette.ComponentSource;
import io.datakitchen.ide.editors.neweditors.palette.ConnectorPalette;
import io.datakitchen.ide.model.*;
import io.datakitchen.ide.ui.*;
import io.datakitchen.ide.util.RecipeUtil;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.text.PlainDocument;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DataMapperNodeView
        extends JPanel
        implements DataMapperModelListener,
            ConnectionListViewListener {

    private final JEditorPane description = new JEditorPane();
    private final JPanel editorPanel = new JPanel();
    private final ConnectionListView sources = new ConnectionListView();
    private final ConnectionListView sinks = new ConnectionListView();
    private final JPanel mainPanel = new JPanel();
    private final ArrowsGlassPane arrowsPanel = new ArrowsGlassPane(this);
    private RunRequestHandler runRequestHandler;

    private DataMapperModel model;

    public DataMapperNodeView(DataMapperModel model, ComponentSource componentSource){
        sources.setEmptyText("Drag and drop here connectors\nto create source connections");
        sinks.setEmptyText("Drag and drop here connectors\nto create sink connections");
        sources.setPreferredSize(new Dimension(250,100));
        sources.setBorder(
            new TitledBorder(
                LineBorder.right(),
                "Sources",
                TitledBorder.RIGHT,
                TitledBorder.DEFAULT_POSITION
            )
        );

        sinks.setPreferredSize(new Dimension(250,100));
        sinks.setBorder(
                new TitledBorder(LineBorder.left(),"Sinks"));

        this.editorPanel.setLayout(new BorderLayout());
        this.editorPanel.add(sources, BorderLayout.WEST);
        JPanel dummyPanel = new JPanel(new BorderLayout());
        dummyPanel.setPreferredSize(new Dimension(50,200));
        dummyPanel.add(new Arrow(""), BorderLayout.CENTER);
        this.editorPanel.add(dummyPanel,BorderLayout.CENTER);
        this.editorPanel.add(sinks, BorderLayout.EAST);

        this.mainPanel.setLayout(new BorderLayout());

        JScrollPane scroll = new JBScrollPane(description, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setPreferredSize(new Dimension(100,100));
        JPanel descriptionArea = new JPanel(new BorderLayout());
        descriptionArea.add(scroll, BorderLayout.CENTER);
        descriptionArea.setBorder(new TitledBorder(LineBorder.top(), "Description"));

        JPanel contents = new JPanel(new BorderLayout());
        contents.add(descriptionArea, BorderLayout.NORTH);
        editorPanel.setBorder(new TitledBorder(LineBorder.top(),"Mappings"));
        contents.add(editorPanel, BorderLayout.CENTER);

        JPanel centerHolder = new JPanel(new FlowLayout(FlowLayout.CENTER));
        centerHolder.add(contents);

        CompoundPanel compoundPanel = new CompoundPanel(arrowsPanel, centerHolder);

        editorPanel.setPreferredSize(new Dimension(600,600));

        this.mainPanel.add(compoundPanel, BorderLayout.CENTER);

        ConnectorPalette palette = new ConnectorPalette();
        palette.setComponentSource(componentSource);
        palette.setPreferredSize(new Dimension(200,200));
        this.mainPanel.add(palette, BorderLayout.EAST);

        sources.setKeyViewFactory(this::createSourceKeyView);
        sources.setViewFactory(this::createDataSourceConnectionView);
        sinks.setKeyViewFactory(this::createSinkKeyView);
        sources.addConnectionListViewListener(this);
        sinks.addConnectionListViewListener(this);

        setLayout(new BorderLayout());
        add(mainPanel, BorderLayout.CENTER);
        setModel(model);
        description.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                model.setDescription(description.getText());
            }
        });
    }

    public RunRequestHandler getRunRequestHandler() {
        return runRequestHandler;
    }

    public void setRunRequestHandler(RunRequestHandler runRequestHandler) {
        this.runRequestHandler = runRequestHandler;
        sources.setRunRequestHandler(runRequestHandler);
    }

    private KeyView createSourceKeyView(Connection connection, Key key){
        SourceFileView keyView = new SourceFileView(key, this::getSourceFileActions);
        if (key instanceof DataSourceSqlKey){
            keyView.setDoubleClickAction(
                new SimpleAction("", e->{
                   openFile((DataSourceSqlKey)key);
                })
            );
            keyView.setToolTipText("Double click to open the file");
        } else {
            keyView.setDoubleClickAction(
                    new SimpleAction("", e-> {
                        editSourceKey(keyView, (DataSourceFileKey)key);
                    })
            );
        }
        return keyView;
    }

    private void openFile(DataSourceSqlKey key) {
        Module module = model.getModule();
        VirtualFile recipeFolder = RecipeUtil.recipeFolder(module);
        VirtualFile resourcesFolder = recipeFolder.findChild("resources");
        if (resourcesFolder == null){
            return;
        }
        VirtualFile file = resourcesFolder.findFileByRelativePath(key.getFile());
        if (file == null){
            return;
        }
        FileEditorManager.getInstance(module.getProject()).openFile(file, true);
    }

    private KeyView createSinkKeyView(Connection connection, Key key){
        SinkFileView keyView = new SinkFileView( key, this::getSinkFileActions);

        if (key instanceof DataSinkFileKey){
            keyView.setDoubleClickAction(
                new SimpleAction("", e -> {
                    editSinkKey(keyView, (DataSinkFileKey)key);
                })
            );
        } else if (key instanceof DataSinkSqlKey){
            keyView.setDoubleClickAction(
                new SimpleAction("", e-> {
                    editSinkSqlKey(keyView, (DataSinkSqlKey)key);
                })
            );
        }

        return keyView;
    }

    private void editSourceKey(SourceFileView keyView, DataSourceFileKey key) {
        InlineEditorPopup.edit(
            keyView,
            new TextFieldInlineEditor(key.getFile()),
            key::setFile,
            s -> validateWildcards(key, s)
        );
    }

    private boolean validateWildcards(DataSourceFileKey key, String file){
        return true; // TODO
    }

    private void editSinkSqlKey(SinkFileView keyView, DataSinkSqlKey key) {
        InlineEditorPopup.edit(
                keyView,
                new TextFieldInlineEditor(key.getName()),
                key::setName
        );
    }

    private void editSinkKey(SinkFileView keyView, DataSinkFileKey key) {
        Key sourceKey = key.getSourceKey();
        if (sourceKey.getConnection().getConnector().getConnectorType().getNature() == ConnectorNature.SQL){
            InlineEditorPopup.edit(
                keyView,
                new SqlSinkFileEditor(key.getFile()),
                key::setFile
            );
        } else {
            InlineEditorPopup.edit(
                keyView,
                new TextFieldInlineEditor(key.getFile()),
                key::setFile
            );
        }
    }

    private ConnectionView createDataSourceConnectionView(ConnectionListView connectionListView, Connection connection) {
        ConnectorNature nature = connection.getConnector().getConnectorType().getNature();

        switch (nature){
            case FILE:
                return new DataSourceFileConnectionView(connectionListView, connection, this::addSourceFileKey);
            case SQL:
                return new DataSourceSqlConnectionView(connectionListView, connection, this::addSourceSqlFile);
            default:
                return new ConnectionView(connectionListView, connection);
        }

    }

    private void addSourceFileKey(Connection connection){
        JTextField field = new JTextField();
        field.setPreferredSize(new Dimension(200,28));
        JBPopup popup = JBPopupFactory
                .getInstance()
                .createComponentPopupBuilder(field, field)
                .setFocusable(true)
                .setRequestFocus(true)
                .createPopup();
        field.addActionListener(e -> {
            doAddSourceFile(connection, field.getText());
            popup.cancel();
        });
        Point point = sources.getHookForNewFileEntry(connection);
        popup.showInScreenCoordinates(this, point);
    }

    private void addSourceSqlFile(Connection connection) {
        EnterFileNameDialog dialog = new EnterFileNameDialog(model.getModule());
        if (dialog.showAndGet()){
            String fileName = dialog.getName();
            ApplicationManager.getApplication().runWriteAction(()->{
                try {
                    VirtualFile newFile = RecipeUtil
                            .recipeFolder(this.model.getModule())
                            .findChild("resources")
                            .createChildData(DataMapperNodeView.this, fileName);
                    connection.addKeyFromFile(newFile);
                }catch (IOException ex){
                    ex.printStackTrace();
                }
            });
        }
    }

    private void doAddSourceFile(Connection connection, String path) {
        if (!path.contains("*") || connection.getKeys().stream().noneMatch(k -> k.getName().contains("*"))){
            model.createSourceFileKey(connection,path);
        }
    }

    public DataMapperModel getModel() {
        return model;
    }

    public void setModel(io.datakitchen.ide.model.DataMapperModel model) {
        if (this.model != null){
            this.model.removeDataMapperModelListener(this);
        }
        this.model = model;
        if (this.model != null){
            this.model.addDataMapperModelListener(this);
            this.sources.setConnectionList(this.model.getDataSources());
            this.sinks.setConnectionList(this.model.getDataSinks());
            this.description.setText(this.model.getDescription());
            this.description.getDocument().putProperty(PlainDocument.tabSizeAttribute,4);
        }
    }

    public Point getHookPointForSourceFile(String sourceName, String sourceFile) {
        ConnectionView connectionView = sources.findConnectorByName(sourceName);
        KeyView fileView = connectionView.findFileByName(sourceFile);

        Point hookPoint = fileView.getHookPoint();
        hookPoint.x+=fileView.getLocationOnScreen().x-getLocationOnScreen().x;
        hookPoint.y+=fileView.getLocationOnScreen().y-getLocationOnScreen().y;

        return hookPoint;
    }

    public Point getHookPointForSinkFile(String sinkName, String sinkFile) {
        ConnectionView connectionView = sinks.findConnectorByName(sinkName);
        KeyView fileView = connectionView.findFileByName(sinkFile);

        Point hookPoint = fileView.getHookPoint();
        hookPoint.x+=fileView.getLocationOnScreen().x-getLocationOnScreen().x;
        hookPoint.y+=fileView.getLocationOnScreen().y-getLocationOnScreen().y;

        return hookPoint;
    }

    private Action[] getSourceFileActions(LabelWithActions requester) {
        return new Action[]{};
    }

    private Action[] getSinkFileActions(LabelWithActions requester) {

        SinkKey key = (SinkKey) ((SinkFileView)requester).getKey();
        Connection connection = ((SinkFileView)requester).getKey().getConnection();

        List<Action> actions = new ArrayList<>();

        ConnectorNature sinkNature = connection.getConnector().getConnectorType().getNature();
        ConnectorNature sourceNature = key.getSourceKey().getConnection().getConnector().getConnectorType().getNature();

        if (sinkNature == ConnectorNature.FILE
            && sourceNature == ConnectorNature.SQL){

            actions.addAll(
                List.of(
                        new SimpleAction("Dump as CSV", e -> key.setDumpType(DumpType.CSV)),
                        new SimpleAction("Dump as Binary", e -> key.setDumpType(DumpType.BINARY)),
                        new SimpleAction("Dump as JSON", e -> key.setDumpType(DumpType.JSON))
                )
            );
        } else if (sinkNature == ConnectorNature.SQL){
            actions.add(new SimpleAction("Details", e -> editSqlSinkKeyDetails(key)));
        }

        return actions.toArray(new Action[actions.size()]);
    }

    private void editSqlSinkKeyDetails(SinkKey key) {
        SqlSinkKeyDetailsDialog dialog = new SqlSinkKeyDetailsDialog((DataSinkSqlKey) key);
        if (dialog.showAndGet()){
            dialog.writeToKey((DataSinkSqlKey) key);
        }
    }

    @Override
    public void mappingsAdded(DataMapperModelEvent event) {
        arrowsPanel.repaint();
    }

    @Override
    public void mappingsRemoved(DataMapperModelEvent event) {
        arrowsPanel.repaint();
    }


}
