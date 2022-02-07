package io.datakitchen.ide.editors.neweditors;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.util.IconLoader;
import com.intellij.ui.RoundedLineBorder;
import io.datakitchen.ide.dialogs.VariableEditorDialog;
import io.datakitchen.ide.model.*;
import io.datakitchen.ide.ui.*;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import java.awt.*;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

public class ConnectionView
        extends JPanel
        implements LabelWithActions.ActionSupplier,
            DropTargetListener,
            DragGestureListener,
            ConnectionListener,
            KeyListener {

    private final LabelWithActions label;
    private RunRequestHandler runRequestHandler;
    protected final JPanel keys = new JPanel(new VerticalStackLayout());
    private final VariablesHolder variables = new VariablesHolder();
    private final TestsHolder tests = new TestsHolder();
    private final DragSource dragSource = new DragSource();
    private final ConnectionListView connectionListView;
    private final Connection connection;
    private KeyViewFactory keyViewFactory;
    private final Set<Action> actions = new LinkedHashSet<>();


    public ConnectionView(
            ConnectionListView connectionListView,
            Connection connection){
        this.connectionListView = connectionListView;
        this.connection = connection;
        this.connection.addConnectionListener(this);
        label = new LabelWithActions(null,null,this);
        label.setHighlightOnHover(false);
        label.setText(connection.getName());
        label.setIcon(IconLoader.getIcon("/icons/connectors/"+connection.getConnector().getConnectorType().getName()+"_small.svg",getClass()));
        label.addMouseListener(new DoubleClickHandler(this::editConnector));
        setBorder(new CompoundBorder(new RoundedLineBorder(getForeground(),10,1), UIUtil.EMPTY_BORDER_10x10));
        setLayout(new VerticalStackLayout());

        label.setBorder(LineBorder.bottom());

        DropTarget dropTarget = new DropTarget();
        try {
            dropTarget.addDropTargetListener(this);
        }catch(TooManyListenersException ignored){}
        dropTarget.setActive(true);
        keys.setDropTarget(dropTarget);
        keys.setPreferredSize(new Dimension(20,20));
        keys.setBorder(UIUtil.EMPTY_BORDER_5x5);

        add(label);
        add(keys);
        add(tests);
        add(variables);
    }

    public void build() {
        loadFiles();
        loadVariables();
        loadTests();
    }

    public RunRequestHandler getRunRequestHandler() {
        return runRequestHandler;
    }

    public void setRunRequestHandler(RunRequestHandler runRequestHandler) {
        this.runRequestHandler = runRequestHandler;
    }

    public void addActions(Collection<Action> actions){
        this.actions.addAll(actions);
    }

    public Connection getConnection(){
        return connection;
    }

    @Override
    public Action[] getActions(LabelWithActions labelWithActions) {
        List<Action> actions = new ArrayList<>(this.actions);
        if (runRequestHandler != null){
            actions.add(new SimpleAction(AllIcons.Actions.Execute, "Run", "Run Connection", e->runConnection(connection)));
        }
        actions.addAll(List.of(
                new SimpleAction(EditorIcons.TEST, "Add test","", e -> addTest(connection)),
                new SimpleAction(EditorIcons.VARIABLE, "Add variable","", e -> addVariableFromSource(connection)),
                new SimpleAction("Remove",this::removeConnection)
        ));
        return actions.toArray(Action[]::new);
    }

    private void runConnection(Connection connection) {
        runRequestHandler.runConnection(connection);
    }

    private void addTest(Connection connection) {

        TestEditorDialog editorDialog = new TestEditorDialog(connection.getModel().getModule(), connection.getModel().getNodeName());
        if (editorDialog.showAndGet()){
            connection.addTest(editorDialog.createTest());
        }
    }

    private void addVariableFromSource(Connection connection) {
        DataType dataType = connection.getDataType();

        VariableEditorDialog dialog = new VariableEditorDialog(dataType, VariableEditorDialog.Type.CONNECTOR);
        if (dialog.showAndGet()){
            connection.addVariable(dialog.getVariable());
        }
    }

    private void removeConnection(ActionEvent e){
        connectionListView.getConnectionList().removeConnection(connection);
    }

    public KeyViewFactory getFileViewFactory() {
        return keyViewFactory;
    }

    public void setFileViewFactory(KeyViewFactory keyViewFactory) {
        this.keyViewFactory = keyViewFactory;
    }

    private void editConnector(MouseEvent e) {
        String oldName = connection.getName();

        InlineEditorPopup.edit(
                label,
                new TextFieldInlineEditor(oldName),
                s -> changeName(oldName, s)
        );
    }

    private void changeName(String oldName, String newName) {
        connection.setName(newName);
        doLayout();
        repaint();
    }

    @Override
    public void dragEnter(DropTargetDragEvent dtde) {
        dtde.acceptDrag(DnDConstants.ACTION_COPY);
    }

    @Override
    public void dragOver(DropTargetDragEvent dtde) {}

    @Override
    public void dropActionChanged(DropTargetDragEvent dtde) {}

    @Override
    public void dragExit(DropTargetEvent dte) {}

    @Override
    public void drop(DropTargetDropEvent dtde) {
        dtde.acceptDrop(DnDConstants.ACTION_COPY);
        Transferable t = dtde.getTransferable();
        connection.addKeyFromTransferable(t);
    }


    @Override
    public void dragGestureRecognized(DragGestureEvent dge) {

        KeyView dataFile = (KeyView)dge.getComponent();
        Key key = dataFile.getKey();

        KeyReference ref = new KeyReference(this.connection.getName(),key.getName());

        dge.startDrag(
            UIUtil.toCursor(dataFile),
            new ObjectTransferable(ref, KeyReference.FLAVOR)
        );
    }

    public List<KeyView> getKeyViews(){
        return Arrays.stream(keys.getComponents())
                .filter(c -> c instanceof KeyView)
                .map(c -> (KeyView)c)
                .collect(Collectors.toList());
    }

    public KeyView findFileByName(String fileName) {
        return getKeyViews()
                .stream().filter(k -> k.getKey().getName().equals(fileName))
                .findFirst()
                .orElse(null);
    }

    private void removeVariable(VariableView view) {
        RuntimeVariable variable = view.getVariable();
        for (KeyView keyView: getKeyViews()){
            Key key = keyView.getKey();
            if (key.getVariables().contains(variable)){
                key.removeVariable(variable);
                break;
            }
        }
        connection.removeVariable(view.getVariable());
    }

    private void removeTest(LabelWithActions source) {
        TestView view = (TestView) source;
        connection.removeTest(view.getTest());
    }


    public Point getHookForNewFileEntry() {
        Point p = keys.getLocationOnScreen();
        p.y+= keys.getHeight();
        return p;
    }

    @Override
    public void nameChanged(ConnectionEvent event) {
        label.setText(connection.getName());
    }

    @Override
    public void testAdded(ConnectionEvent event) {
        Test test = event.getTest();
        doAddTest(test);
    }

    @Override
    public void testChanged(ConnectionEvent event) {
        tests.repaint();
    }

    @Override
    public void testRemoved(ConnectionEvent event) {
        Test test = event.getTest();
        tests.remove(tv -> tv.getTest().equals(test));
    }

    @Override
    public void variableAdded(ConnectionEvent event) {
        RuntimeVariable variable = event.getVariable();
        doAddVariable(null, variable);
    }

    @Override
    public void variableAdded(KeyEvent event) {
        Key key = (Key)event.getSource();
        RuntimeVariable variable = event.getVariable();
        if (!variable.isTestVariable()) {
            doAddVariable(key, variable);
        }
    }

    @Override
    public void variableRemoved(KeyEvent event) {
        Key key = (Key)event.getSource();
        RuntimeVariable variable = event.getVariable();
        variables.remove(v -> key.equals(v.getKey())
                && variable.equals(v.getVariable()));
    }

    @Override
    public void keyChanged(KeyEvent event) {
        getKeyViews()
            .stream()
            .filter(k -> k.getKey().equals(event.getSource()))
            .findFirst().ifPresent(KeyView::updateView);
    }

    @Override
    public void variableRemoved(ConnectionEvent event) {
        variables.remove(v -> event.getVariable().equals(v.getVariable()));
    }

    @Override
    public void keyAdded(ConnectionEvent event) {
        doAddKey(event.getKey());
    }

    @Override
    public void keyRemoved(ConnectionEvent event) {
        for (KeyView view: getKeyViews()){
            if (view.getKey().equals(event.getKey())){
                keys.remove(view);
                keys.validate();
                keys.repaint();
                break;
            }
        }
    }
    private void loadFiles(){
        for (Key key:connection.getKeys()){
            doAddKey(key);
        }
    }

    private void loadVariables() {
        connection.getVariables()
                .stream()
                .filter(v -> !v.isTestVariable())
                .forEach(v -> doAddVariable(null,v));

        for (Key key: connection.getKeys()){
            key.getVariables()
                .stream()
                .filter(v -> !v.isTestVariable())
                .forEach(v -> doAddVariable(key, v));
        }

    }

    private void loadTests() {
        for (Test test:connection.getTests()){
            doAddTest(test);
        }
    }

    private void doAddKey(Key key){
        key.addKeyListener(this);
        KeyView fileView = keyViewFactory.createKeyView(this.connection, key);
        fileView.addActions(createActionForKey(key));
        dragSource.createDefaultDragGestureRecognizer(fileView,DnDConstants.ACTION_COPY,this);
        insertNewKey(fileView);
    }

    protected void insertNewKey(KeyView keyView){
        keys.add(keyView);
        revalidate();
        repaint();
    }

    private List<Action> createActionForKey(Key key) {
        return List.of(
            new SimpleAction(
                    EditorIcons.TEST,
                    "Add Test", "",
                    e-> addSource(connection,key)
            ),
            new SimpleAction(
                    EditorIcons.VARIABLE,
                    "Add Variable", "",
                    e-> addVariableFromSourceFile(connection, key)
            ),
            new SimpleAction("Remove", e -> removeKey(key))
        );
    }


    private void addSource(Connection connection, Key key) {

        DataType dataType = connection.getDataType();

        FileTestEditorDialog editorDialog = new FileTestEditorDialog(dataType, key);
        if (editorDialog.showAndGet()){
            connection.addTest(editorDialog.createTest());
        }
    }

    private void addVariableFromSourceFile(Connection connection, Key key) {
        DataType dataType = connection.getDataType();

        VariableEditorDialog dialog = new VariableEditorDialog(dataType, VariableEditorDialog.Type.KEY);
        if (dialog.showAndGet()){
            key.addVariable(dialog.getVariable());
        }
    }

    private void removeKey(Key key) {
        Connection connection = key.getConnection();
        connection.removeKey(key);
    }

    private void doAddTest(Test test){
        TestView testView = new TestView(test, this::getTestActions);
        testView.setDoubleClickAction(new SimpleAction("", __ -> editTest(test)));
        tests.addItem(testView);
    }

    private Action[] getTestActions(TestView source){
        return new Action[]{
                new SimpleAction("Edit", __ -> editTest(source.getTest())),
                new SimpleAction("Remove", e ->removeTest(source))
        };
    }

    private void editTest(Test test) {
        if (test instanceof FileTest){
            FileTest fileTest = (FileTest)test;

            FileTestEditorDialog dialog = new FileTestEditorDialog(this.connection.getDataType(), fileTest);
            if (dialog.showAndGet()){
                dialog.updateTest(fileTest);
            }
        } else {
            TestEditorDialog dialog = new TestEditorDialog(connection.getModel().getModule(), connection.getModel().getNodeName(), test);
            if (dialog.showAndGet()){
                dialog.updateTest(test);
            }
        }
        this.connection.updateTest(test);
    }

    private void doAddVariable(Key key, RuntimeVariable variable){
        VariableView variableView;
        if (key != null){
            variableView = new VariableView(variable, key, this::getVariableActions);
        } else {
            variableView = new VariableView(variable, this::getVariableActions);
        }
        variableView.setDoubleClickAction(new SimpleAction("", e -> editVariable(variableView)));
        variables.addItem(variableView);
    }

    private Action[] getVariableActions(VariableView source){
        return new Action[]{
                new SimpleAction("Edit", e -> editVariable(source)),
                new SimpleAction("Remove", e -> removeVariable(source))
        };
    }

    private void editVariable(VariableView variableView) {
        VariableEditorDialog.Type type = variableView.getKey() == null
                ? VariableEditorDialog.Type.CONNECTOR
                : VariableEditorDialog.Type.KEY;

        VariableEditorDialog dialog = new VariableEditorDialog(getConnection().getDataType(), type, variableView.getVariable());

        if (dialog.showAndGet()){
            dialog.updateVariable(variableView.getVariable());
        }
    }


}
