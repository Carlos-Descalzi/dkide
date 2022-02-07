package io.datakitchen.ide.editors.neweditors.action;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.components.JBScrollPane;
import io.datakitchen.ide.dialogs.EnterFileNameDialog;
import io.datakitchen.ide.editors.neweditors.*;
import io.datakitchen.ide.editors.neweditors.palette.ComponentSource;
import io.datakitchen.ide.editors.neweditors.palette.ConnectorPalette;
import io.datakitchen.ide.model.*;
import io.datakitchen.ide.ui.LineBorder;
import io.datakitchen.ide.ui.SimpleAction;
import io.datakitchen.ide.util.RecipeUtil;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.text.PlainDocument;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.IOException;
import java.util.List;

public class ActionNodeView extends JPanel implements NodeModelListener {
    private final JEditorPane description = new JEditorPane();
    private final ConnectionListView connectionListView;
    private final ActionNodeModel model;
    private final NodeTestsView tests;
    private RunRequestHandler runRequestHandler;

    public ActionNodeView(ActionNodeModel model, ComponentSource componentSource){
        this.model = model;
        setLayout(new BorderLayout());

        ConnectorPalette palette = new ConnectorPalette(ConnectorNature.SQL);
        palette.setComponentSource(componentSource);
        add(palette, BorderLayout.EAST);

        connectionListView = new ConnectionListView();
        connectionListView.setEmptyText("Drag and drop here connectors\nto run SQL scripts");
        connectionListView.setViewFactory(this::createConnectionView);
        connectionListView.setConnectionList(model.getConnectionList());
        connectionListView.setPreferredSize(new Dimension(500,300));

        JScrollPane scroll = new JBScrollPane(description, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setPreferredSize(new Dimension(100,100));
        JPanel descriptionArea = new JPanel(new BorderLayout());
        descriptionArea.add(scroll, BorderLayout.CENTER);
        description.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                model.setDescription(description.getText());
            }
        });

        tests = new NodeTestsView(model, this::onAddTest);

        JPanel content = new JPanel(new BorderLayout());
        content.add(descriptionArea, BorderLayout.NORTH);
        content.add(connectionListView, BorderLayout.CENTER);
        content.add(tests, BorderLayout.SOUTH);

        JPanel centerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        centerPanel.add(content, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        descriptionArea.setBorder(new TitledBorder(LineBorder.top(), "Description"));
        connectionListView.setBorder(new TitledBorder(LineBorder.top(),"Connections"));
        tests.setBorder(new TitledBorder(LineBorder.top(),"Node Tests"));
        loadView();

        model.addNodeModelListener(this);
    }

    public RunRequestHandler getRunRequestHandler() {
        return runRequestHandler;
    }

    public void setRunRequestHandler(RunRequestHandler runRequestHandler) {
        this.runRequestHandler = runRequestHandler;
        this.connectionListView.setRunRequestHandler(runRequestHandler);
    }

    private void loadView() {
        description.setText(model.getDescription());
        description.getDocument().putProperty(PlainDocument.tabSizeAttribute,4);
    }

    private void onAddTest() {
        TestEditorDialog dialog = new TestEditorDialog(model.getModule(), model.getNodeName());
        if (dialog.showAndGet()){
            model.addTest(dialog.createTest());
        }
    }

    private ConnectionView createConnectionView(ConnectionListView connectionListView, Connection connection) {

        ConnectionView view = new DataSourceSqlConnectionView(connectionListView, connection,this::createSQLFile);
        view.setFileViewFactory(this::createKeyView);
        view.setBorder(LineBorder.bottom(getBackground().brighter()));
        return view;
    }

    private void createSQLFile(Connection connection) {
        EnterFileNameDialog dialog = new EnterFileNameDialog(model.getModule());
        if (dialog.showAndGet()){
            String fileName = dialog.getName();
            ApplicationManager.getApplication().runWriteAction(()->{
                try {
                    VirtualFile newFile = RecipeUtil
                            .recipeFolder(this.model.getModule())
                            .findChild("resources")
                            .createChildData(ActionNodeView.this, fileName);
                    connection.addKeyFromFile(newFile);

                }catch (IOException ex){
                    ex.printStackTrace();
                }
            });
        }
    }

    private KeyView createKeyView(Connection connection, Key key) {
        ActionKey actionKey = (ActionKey) key;
        ActionKeyView keyView = new ActionKeyView(actionKey, v -> new Action[0]);

        keyView.setDoubleClickAction(new SimpleAction("", this::openFile));
        keyView.addActions(List.of(
                new SimpleAction("DML/DDL (no results)", e -> this.setQueryTypeNoResult(actionKey)),
                new SimpleAction("Scalar result", e-> this.runQueryAsScalar(actionKey))
        ));

        return keyView;
    }

    private void runQueryAsScalar(ActionKey key) {
        key.setQueryType(QueryType.EXECUTE_SCALAR);
    }

    private void setQueryTypeNoResult(ActionKey key) {
        key.setQueryType(QueryType.EXECUTE_STATEMENT);
    }

    private void openFile(ActionEvent event) {
        ActionKey key =(ActionKey) ((ActionKeyView)event.getSource()).getKey();
        Module module = model.getModule();
        VirtualFile recipeFolder = RecipeUtil.recipeFolder(module);
        VirtualFile resourcesFolder = recipeFolder.findChild("resources");
        if (resourcesFolder == null){
            return;
        }
        VirtualFile file = resourcesFolder.findFileByRelativePath(key.getName());
        if (file == null){
            return;
        }
        FileEditorManager.getInstance(module.getProject()).openFile(file, true);
    }

}
