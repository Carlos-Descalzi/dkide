package io.datakitchen.ide.editors.graph;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.components.JBScrollPane;
import io.datakitchen.ide.Constants;
import io.datakitchen.ide.builder.NodeBuilder;
import io.datakitchen.ide.dialogs.ConditionEditorDialog;
import io.datakitchen.ide.dialogs.NewNodeDialog;
import io.datakitchen.ide.editors.DocumentChangeEvent;
import io.datakitchen.ide.editors.DocumentChangeListener;
import io.datakitchen.ide.editors.graph.paste.PasteFileHandler;
import io.datakitchen.ide.editors.graph.paste.PasteHandler;
import io.datakitchen.ide.editors.graph.paste.PasteStringHandler;
import io.datakitchen.ide.model.Condition;
import io.datakitchen.ide.model.NodeType;
import io.datakitchen.ide.run.NodeRunner;
import io.datakitchen.ide.tools.NodePalette;
import io.datakitchen.ide.ui.EventSupport;
import io.datakitchen.ide.ui.SimpleAction;
import io.datakitchen.ide.util.RecipeUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.*;
import java.awt.event.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class GraphEditor extends JPanel implements DropTargetListener, Disposable {

    private final Project project;
    private final VirtualFile recipeFolder;
    private final Module module;

    private VariationGraph currentGraph;

    private final GraphView graphView = new GraphView();
    private final EventSupport<DocumentChangeListener> eventSupport = EventSupport.of(DocumentChangeListener.class);
    private final Action addNoOpNode = new AddNodeAction(RecipeUtil.NODE_TYPE_NOOP,"Add NoOp node",false, this::addNode);
    private final Action addContainerNode = new AddNodeAction(RecipeUtil.NODE_TYPE_CONTAINER,"Add Container node",false, this::addNode);
    private final Action addScriptNode = new AddNodeAction(RecipeUtil.NODE_TYPE_CONTAINER_SUBTYPE_SCRIPT,"Add Script node",false, this::addNode);
    private final Action addActionNode = new AddNodeAction(RecipeUtil.NODE_TYPE_ACTION, "Add Action node",false, this::addNode);
    private final Action addDataMapperNode = new AddNodeAction(RecipeUtil.NODE_TYPE_DATA_MAPPER,"Add Data mapper node",false, this::addNode);
    private final Action addIngredientNode = new AddNodeAction(RecipeUtil.NODE_TYPE_INGREDIENT,"Add Ingredient node",false, this::addNode);
    private final Action addConditionalNode = new SimpleAction("Add conditional node", this::addConditionalNode);

    private final Action runNodeAction = new SimpleAction(AllIcons.Actions.Run_anything, "Run node", "Run Node", this::runNode);
    private final Action toggleBreakpointAction = new SimpleAction("Toggle breakpoint", this::toggleBreakpoint);
    private final FocusListener focusListener = new FocusAdapter() {
        @Override
        public void focusLost(FocusEvent e) {
            graphUpdated();
        }
    };
    private final GraphModelListener graphListener = new GraphModelListener() {
        @Override
        public void nodeAdded(GraphModelEvent event) {
            graphUpdated();
        }

        @Override
        public void edgeAdded(GraphModelEvent event) {
            graphUpdated();
        }

        @Override
        public void nodeRemoved(GraphModelEvent event) {
            graphUpdated();
        }

        @Override
        public void edgeRemoved(GraphModelEvent event) {
            graphUpdated();
        }

        @Override
        public void nodeChanged(GraphModelEvent event) {
            updateNode(event.getOldName(), event.getNewName());
            graphUpdated();
        }
    };

    private final List<PasteHandler> pasteHandlers;

    private final Set<String> nodeBreakpoints = new HashSet<>();

    public GraphEditor(Project project, VirtualFile recipeFolder){
        this.project = project;
        this.recipeFolder = recipeFolder;
        this.module = ModuleUtil.findModuleForFile(recipeFolder,project);
        pasteHandlers = Arrays.asList(
                new PasteFileHandler(this, module),
                new PasteStringHandler(this, module)
        );
        graphView.setRecipeFolder(recipeFolder);
        graphView.setDropTarget(new DropTarget());
        try {
            graphView.getDropTarget().addDropTargetListener(this);
        }catch (Exception ignored){}
        graphView.getDropTarget().setActive(true);
        setLayout(new BorderLayout());

        add(new JBScrollPane(graphView, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED),BorderLayout.CENTER);
        NodePalette palette = new NodePalette(project);
        add(palette, BorderLayout.EAST);
        Disposer.register(this, palette);

        graphView.getActionMap().put("new-node", new SimpleAction("Add node",this::showPopupFromAction));
        graphView.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.CTRL_DOWN_MASK| KeyEvent.ALT_DOWN_MASK),"new-node");
        graphView.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()){
                    showPopup(e);
                }
            }
        });
        graphView.addNodeActions(
                runNodeAction,
                toggleBreakpointAction
        );
        graphView.addGraphViewListener(new GraphViewListener() {
            @Override
            public void nodeOpenRequested(GraphViewEvent event) {
                openNodeNotebook(event.getNode());
            }
        });
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                graphView.layoutGraph();
            }

            @Override
            public void componentShown(ComponentEvent e) {
                graphView.layoutGraph();
            }
        });
    }

    @Override
    public void dispose() {

    }

    private void updateNode(String oldName, String newName) {
        VirtualFile nodeFolder = recipeFolder.findChild(oldName);
        ApplicationManager.getApplication().runWriteAction(()->{
            try {
                assert nodeFolder != null;
                nodeFolder.rename(this, newName);
            }catch (Exception ex){
                ex.printStackTrace();
            }
        });
    }

    private void openNodeNotebook(GraphModel.Node node) {
        VirtualFile nodeFolder = recipeFolder.findChild(node.getName());
        if (nodeFolder != null){
            VirtualFile notebookFile = nodeFolder.findChild(Constants.FILE_NOTEBOOK_JSON);
            if (notebookFile != null){
                FileEditorManager.getInstance(project).openFile(notebookFile, true);
            }
        }

    }

    private void toggleBreakpoint(ActionEvent event) {
        String node = graphView.getSelectedNodeName();

        if (node != null){
            if (nodeBreakpoints.contains(node)){
                nodeBreakpoints.remove(node);
            } else {
                nodeBreakpoints.add(node);
            }
        }
        graphView.repaint();
    }

    public void setEnabled(boolean enabled){
        super.setEnabled(enabled);
        graphView.setEnabled(enabled);
    }

    public void addDocumentChangeListener(DocumentChangeListener listener){
        this.eventSupport.addListener(listener);
    }
    public void removeDocumentChangeListener(DocumentChangeListener listener){
        this.eventSupport.removeListener(listener);
    }

    private void showPopupFromAction(ActionEvent event) {
        Point p = graphView.getLocationOnScreen();
        doShowPopup(p.x+graphView.getWidth()/2,p.y+graphView.getHeight()/2);
    }

    private void showPopup(MouseEvent e) {
        doShowPopup(e.getX(),e.getY());
    }

    private void doShowPopup(int x, int y){
        JPopupMenu menu  =new JPopupMenu();
        menu.add(addNoOpNode);
        menu.add(addActionNode);
        menu.add(addContainerNode);
        menu.add(addScriptNode);
        menu.add(addDataMapperNode);
        menu.add(addIngredientNode);
        JMenu childMenu = new JMenu("Add existing node");
        Set<String> actualNodes =graphView.getModel().getNodes().stream().map((GraphModel.Node::getName)).collect(Collectors.toSet());

        for (String nodeName: RecipeUtil.getRecipeNodes(recipeFolder).stream().sorted().collect(Collectors.toList())){
            if (!actualNodes.contains(nodeName)) {
                childMenu.add(new AddNodeAction(nodeName, nodeName, true, this::addNode));
            }
        }
        menu.add(childMenu);
        menu.addSeparator();
        menu.add(addConditionalNode);
        menu.addSeparator();
        menu.add(new SimpleAction("Layout graph",__-> graphView.layoutGraph()));
        menu.add(new SimpleAction("Paste ...",__-> attemptToPaste()));
        menu.show(this,x,y);

    }


    @Override
    public void dragEnter(DropTargetDragEvent dtde) {
        if (!isValidTransferable(dtde.getTransferable())){
            dtde.rejectDrag();
        }
    }

    @Override
    public void dragOver(DropTargetDragEvent dtde) {

    }

    @Override
    public void dropActionChanged(DropTargetDragEvent dtde) {

    }

    @Override
    public void dragExit(DropTargetEvent dte) {

    }

    @Override
    public void drop(DropTargetDropEvent dtde) {
        if (currentGraph != null) {
            try {
                dtde.acceptDrop(DnDConstants.ACTION_COPY);
                Transferable transferable = dtde.getTransferable();
                handleTransferable(transferable);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
    private void attemptToPaste() {
        Transferable transferable = getToolkit().getSystemClipboard().getContents(this);
        try {
            handleTransferable(transferable);
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    private boolean isValidTransferable(Transferable transferable){
        return pasteHandlers.stream()
                .anyMatch((PasteHandler p)->p.isTransferableSupported(transferable, PasteHandler.Target.GRAPH));
    }

    private void handleTransferable(Transferable transferable) {

        for (PasteHandler pasteHandler: pasteHandlers){
            if (pasteHandler.isTransferableSupported(transferable, PasteHandler.Target.GRAPH)){
                pasteHandler.accept(transferable, this);
                return;
            }
        }
        System.out.println("Unhandled transferable "+transferable);

    }

    public GraphView getGraphView(){
        return this.graphView;
    }

    private void addNode(ActionEvent e){
        AddNodeAction action = (AddNodeAction)e.getSource();
        String nodeType = action.getNodeName();
        boolean existingNode = action.isExistingNode();

        if (!existingNode){
            NewNodeDialog dialog = new NewNodeDialog(nodeType);
            if (dialog.showAndGet()){
                String nodeName = dialog.getNodeName();
                Module module = ModuleUtil.findModuleForFile(recipeFolder, project);

                new NodeBuilder(project)
                        .setModule(module)
                        .setNodeName(nodeName)
                        .setNodeType(nodeType)
                        .build(f ->
                            graphView.getModel().addNode(GraphModel.Node.process(nodeName))
                        );
            }
        } else {
            graphView.getModel().addNode(GraphModel.Node.process(nodeType));
        }
    }

    private void addConditionalNode(ActionEvent e) {
        ConditionEditorDialog editor = new ConditionEditorDialog();
        if (editor.showAndGet()){
            Condition condition = new Condition();
            editor.save(condition);
            Module module = ModuleUtil.findModuleForFile(recipeFolder, project);

            String nodeName = condition.getConditionName().replace("-","_");

            new NodeBuilder(project)
                    .setModule(module)
                    .setNodeName(nodeName)
                    .setNodeType(NodeType.NOOP_NODE.getTypeName())
                    .build(f ->
                        graphView.getModel().addNode(GraphModel.Node.condition(nodeName, condition))
                    );
        }
    }

    public void loadGraph(VariationGraph variationGraph) {
        this.currentGraph = variationGraph;
        disableEvents();
        if (variationGraph != null) {
            graphView.setViewModel(new DescriptionAwareGraphViewModel(project, recipeFolder));
            graphView.setModel(GraphModel.fromJson(variationGraph.getGraph(),variationGraph.getConditions()));
        } else {
            graphView.setModel(new GraphModel(ConditionsCollection.NULL_COLLECTION));
        }
        enableEvents();
        repaint();
    }


    private void enableEvents(){
        GraphModel model = graphView.getModel();
        if (model != null) {
            model.addGraphModelListener(graphListener);
        }
    }
    private void disableEvents(){
        GraphModel model = graphView.getModel();
        if (model != null) {
            model.removeGraphModelListener(graphListener);
        }
    }

    private void graphUpdated() {
        eventSupport.getProxy().documentChanged(new DocumentChangeEvent(this));
    }

    public void saveGraph(VariationGraph variationGraph) {
        variationGraph.setGraph(graphView.getModel().toJSONArray());
    }


    private void runNode(ActionEvent event) {
        if (currentGraph != null) {
            String node = graphView.getSelectedNodeName();
            if (node != null) {
                new NodeRunner(module, recipeFolder.findChild(node))
                        .run();
            }
        }
    }

}
