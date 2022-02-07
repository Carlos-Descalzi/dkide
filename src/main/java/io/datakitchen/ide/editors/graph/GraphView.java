package io.datakitchen.ide.editors.graph;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ui.JBUI;
import io.datakitchen.ide.dialogs.OutcomeEditorDialog;
import io.datakitchen.ide.ui.*;
import io.datakitchen.ide.util.RecipeUtil;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Path2D;
import java.util.List;
import java.util.*;

public class GraphView extends JPanel implements GraphModelListener{

    private static final int NODE_SIZE = 52;
    private static final int CONDITIONAL_NODE_SIZE = 36;
    private final EventSupport<GraphViewListener> eventSupport = EventSupport.of(GraphViewListener.class);
    private GraphModel model;
    private final Map<GraphModel.Node, NodeView> nodeViews = new LinkedHashMap<>();
    private final Map<GraphModel.Edge, EdgeView> edgeViews = new LinkedHashMap<>();
    private NodeView selectedNode;
    private VirtualFile recipeFolder;
    private GraphViewModel viewModel = new DefaultGraphViewModel();
    private boolean editable;
    private List<Action> nodeActions = new ArrayList<>();

    public GraphView(){
        setLayout(null);
        editable = true;
    }

    public VirtualFile getRecipeFolder() {
        return recipeFolder;
    }

    public void setRecipeFolder(VirtualFile recipeFolder) {
        this.recipeFolder = recipeFolder;
    }

    public GraphModel getModel() {
        return model;
    }

    public void setModel(GraphModel model) {
        if (this.model != null){
            this.model.removeGraphModelListener(this);
        }

        this.nodeViews.clear();
        this.edgeViews.clear();
        this.removeAll();

        this.model = model;

        if (this.model != null){
            for (GraphModel.Node node : model.getNodes()) {
                addNode(node);
            }
            for (GraphModel.Edge edge:model.getEdges()){
                addEdge(edge);
            }
            this.layoutGraph();
            this.model.addGraphModelListener(this);
        }
    }

    public GraphViewModel getViewModel() {
        return viewModel;
    }

    public void setViewModel(GraphViewModel viewModel) {
        this.viewModel = viewModel;
        repaint();
    }

    public void addGraphViewListener(GraphViewListener listener){
        this.eventSupport.addListener(listener);
    }

    public void removeGraphViewListener(GraphViewListener listener){
        this.eventSupport.removeListener(listener);
    }

    private void addNode(GraphModel.Node node){
        NodeView nodeView = new NodeView(node, getNodeType(node));
        nodeViews.put(node, nodeView);
        add(nodeView);
        add(new NodeLabel(nodeView),0);
        nodeView.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                onNodePressed(e);
            }
        });
    }

    private void addEdge(GraphModel.Edge edge) {
        NodeView source = nodeViews.get(edge.getFrom());
        NodeView target = nodeViews.get(edge.getTo());
        EdgeView edgeView = new EdgeView(edge, source, target);
        edgeViews.put(edge, edgeView);
        add(edgeView);
        if (edge.getOutcome() != null){
            add(new EdgeLabel(edgeView),0);
        }
        validate();
        repaint();
    }

    private void onNodePressed(MouseEvent e){
        NodeView newNode = (NodeView)e.getSource();

        if (this.selectedNode != null && this.selectedNode != newNode
            && (e.getModifiersEx() & MouseEvent.SHIFT_DOWN_MASK) != 0){
            if (this.selectedNode.node.getType() == GraphModel.NodeType.CONDITION){
                addConditionalEdge(this.selectedNode.node, newNode.node);
            } else {
                model.addEdge(this.selectedNode.node, newNode.node);
            }
        }
        this.selectedNode = (NodeView)e.getSource();
        repaint();
        this.eventSupport.getProxy().nodeSelected(new GraphViewEvent(this, newNode.node));
    }

    private void addConditionalEdge(GraphModel.Node sourceNode, GraphModel.Node targetNode) {
        OutcomeEditorDialog dialog = new OutcomeEditorDialog(sourceNode.getCondition());
        if (dialog.showAndGet()) {
            ConditionOutcome outcome = dialog.getOutcome();
            outcome.getTargetNodes().add(targetNode.getName());
            model.addEdge(this.selectedNode.node, targetNode, outcome);
        }
    }

    private String getNodeType(GraphModel.Node node) {
        try {
            VirtualFile nodeFolder = recipeFolder.findChild(node.getName());
            if (nodeFolder != null) {
                return RecipeUtil.getNodeType(nodeFolder);
            }
        }catch (Exception ignore){
        }
        return null;
    }

    public void layoutGraph(){
        if (model == null){
            return;
        }

        int nodeSizeWithSpace = NODE_SIZE * 2;
        int y = 100;

        for (List<GraphModel.Node> level:getNodesInLevels()) {

            int totalLevelWidth = nodeSizeWithSpace * level.size();

            int startX = (getWidth() - totalLevelWidth)/ 2;

            for (int i = 0; i < level.size(); i++) {
                NodeView nodeView = nodeViews.get(level.get(i));
                Dimension size = nodeView.getPreferredSize();
                nodeView.setBounds(
                        startX+i*nodeSizeWithSpace-(size.width/2),
                        y+size.height/2,
                        size.width,
                        size.height
                );
            }
            y+= (int) (NODE_SIZE * 2.5);
        }

        updateMinimumSize();
    }

    private void updateMinimumSize() {
        Dimension d = new Dimension(0,0);
        for (Component c:getComponents()){
            d.width = Math.max(d.width, c.getX()+c.getWidth());
            d.height = Math.max(d.height, c.getY()+c.getHeight());
        }
        d.width+=100;
        d.height+=100;
        setPreferredSize(d);
    }

    private List<List<GraphModel.Node>> getNodesInLevels() {
        List<List<GraphModel.Node>> result = new ArrayList<>();

        Set<GraphModel.Node> nodes = new LinkedHashSet<>(model.roots());
        result.add(new ArrayList<>(nodes));

        while (nodes.size() > 0){
            Set<GraphModel.Node> level = new LinkedHashSet<>();

            for (GraphModel.Node node:nodes){
                Set<GraphModel.Node> outNodes = model.successors(node);
                level.addAll(outNodes);
            }
            result.add(new ArrayList<>(level));
            nodes = level;
        }

        // Remove nodes referenced in multiple levels.
        for (int i=result.size()-1;i>=0;i--){
            for (GraphModel.Node node: result.get(i)){
                if (i > 0) {
                    for (int j = i - 1; j >= 0; j--) {
                        result.get(j).remove(node);
                    }
                }
            }
        }


        return result;
    }

    @Override
    public void nodeAdded(GraphModelEvent event) {
        addNode(event.getNode());
        this.layoutGraph();
    }

    @Override
    public void edgeAdded(GraphModelEvent event) {
        addEdge(event.getEdge());
    }

    @Override
    public void nodeRemoved(GraphModelEvent event) {
        NodeView view = nodeViews.remove(event.getNode());
        this.remove(view);
        for (Component c:getComponents()){
            if (c instanceof NodeLabel && ((NodeLabel)c).node == view){
                remove(c);
                break;
            }
        }
        repaint();
    }

    @Override
    public void edgeRemoved(GraphModelEvent event) {
        EdgeView view = edgeViews.remove(event.getEdge());
        this.remove(view);
        for (Component c:getComponents()){
            if (c instanceof EdgeLabel && ((EdgeLabel)c).edge == view){
                remove(c);
                break;
            }
        }
        repaint();
    }

    @Override
    public void nodeChanged(GraphModelEvent event) {
        for (Component c:getComponents()){
            if (c instanceof NodeLabel){
                NodeLabel nodeView = (NodeLabel) c;

                for (Map.Entry<GraphModel.Node, NodeView> e: nodeViews.entrySet()){
                    if (e.getKey() == event.getNode()){
                        NodeView v = nodeViews.remove(e.getKey());
                        nodeViews.put(event.getNode(), v);
                        break;
                    }
                }

                if (nodeView.node.node == event.getNode()){
                    nodeView.notifyNodeChanged();
                }
            }
        }
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    public boolean isEditable(){
        return editable;
    }

    public void addNodeActions(Action ... nodeActions) {
        this.nodeActions = Arrays.asList(nodeActions);
    }

    private static class Line {
        private double sx;
        private double sy;
        private double tx;
        private double ty;

        public Line(int sx, int sy, int tx, int ty) {
            this.sx = sx;
            this.sy = sy;
            this.tx = tx;
            this.ty = ty;
        }

        public Line(double sx, double sy, double tx, double ty) {
            this.sx = sx;
            this.sy = sy;
            this.tx = tx;
            this.ty = ty;
        }

        public Line(){
            this(0,0,0,0);
        }

        public boolean contains(int x, int y){
            double d = Math.abs(((x - sx) / (tx - sx)) - ((y - sy) / (ty - sy)));
            // is infinite if rect is perfectly horizontal or vertical.
            return Double.isInfinite(d) || d < 0.2;
        }

        public void set(int sx, int sy, int tx, int ty) {
            this.sx = sx;
            this.sy = sy;
            this.tx = tx;
            this.ty = ty;
        }

        public Line scale(double scale) {
            double dx = (tx - sx) * scale;
            double dy = (ty - sy) * scale;
            return new Line(sx, sy, sx + dx, sy + dy);
        }
    }

    private class EdgeView extends JComponent {
        private final NodeView source;
        private final NodeView target;
        private final GraphModel.Edge edge;
        private final Line line = new Line();

        public EdgeView(GraphModel.Edge edge, NodeView source, NodeView target){
            this.edge = edge;
            this.source = source;
            this.target = target;
            enableEvents(AWTEvent.MOUSE_EVENT_MASK| AWTEvent.MOUSE_MOTION_EVENT_MASK);
            ComponentListener listener = new ComponentAdapter() {
                @Override
                public void componentMoved(ComponentEvent e) {
                    updateBounds();
                }
            };
            source.addComponentListener(listener);
            target.addComponentListener(listener);

            updateBounds();
        }

        public Point getEdgeHookPoint(){
            Line line = this.line.scale(3.0 / 4.0);
            return new Point((int)(getX()+line.tx),(int)(getY()+line.ty));
        }


        public boolean contains(int x, int y) {
            if (x >= 0 && x < getWidth() && y >= 0 && y < getHeight()) {
                return line.contains(x,y);
            }
            return false;
        }
        @Override
        protected void processMouseEvent(MouseEvent e) {
            if (e.getID() == MouseEvent.MOUSE_PRESSED){
                if (e.isPopupTrigger() && GraphView.this.editable){
                    showPopup(e);
                }
            }
            super.processMouseEvent(e);
        }

        private void showPopup(MouseEvent e){
            JPopupMenu popup = new JPopupMenu();
            popup.add(new SimpleAction("Remove edge", this::removeEdge));
            popup.show(this,e.getX(),e.getY());
        }

        private void removeEdge(ActionEvent e){
            GraphView.this.removeEdge(this.edge);
        }

        private void updateBounds() {
            Point ps = source.getHookPoint(target, true);
            Point pt = target.getHookPoint(source, false);

            int sx = (int)Math.min(ps.getX(),pt.getX());
            int sy = (int)Math.min(ps.getY(),pt.getY());
            int tx = (int)Math.max(ps.getX(),pt.getX());
            int ty = (int)Math.max(ps.getY(),pt.getY());

            setBounds(sx-5,sy-5,(tx - sx)+10,(ty - sy)+10);

            line.set(
                    ps.x-getX(),
                    ps.y-getY(),
                    pt.x-getX(),
                    pt.y-getY()
            );
        }

        protected void paintComponent(Graphics g){
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(Color.WHITE);
            g2d.drawLine((int)line.sx,(int)line.sy, (int)line.tx, (int)line.ty);

            g2d.setStroke(new BasicStroke(3));

            double arrowAngle = 30.0 * Math.PI / 180.0;

            double angle = Math.atan2(line.ty - line.sy,line.tx - line.sx);

            double cos1 = Math.cos(angle - arrowAngle);
            double sin1 = Math.sin(angle - arrowAngle);
            double cos2 = Math.cos(angle + arrowAngle);
            double sin2 = Math.sin(angle + arrowAngle);

            Path2D shape = new Path2D.Double();
            shape.moveTo(line.tx,line.ty);
            shape.lineTo(line.tx - cos1 * 10,line.ty - sin1 * 10);
            shape.lineTo(line.tx - cos2 * 10,line.ty - sin2 * 10);
            shape.lineTo(line.tx,line.ty);

            g2d.fill(shape);

        }

    }

    private void notifyOpenNode(GraphModel.Node node){
        if (node.getType() != GraphModel.NodeType.CONDITION) {
            eventSupport.getProxy().nodeOpenRequested(new GraphViewEvent(this, node));
        }
    }

    private class NodeView extends JComponent {
        private final GraphModel.Node node;
        private Icon icon;
        private Point pressPoint;

        public NodeView(GraphModel.Node node, String nodeType){
            this.node = node;

            this.icon = GraphView.this.getViewModel().getIcon(node, nodeType);
            getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE,0),"delete-node");
            getActionMap().put("delete-node",new SimpleAction("delete node",this::removeNode));
            setFocusable(true);
            enableEvents(AWTEvent.MOUSE_EVENT_MASK| AWTEvent.MOUSE_MOTION_EVENT_MASK| AWTEvent.KEY_EVENT_MASK);

            if (node.getType() == GraphModel.NodeType.CONDITION){

                setPreferredSize(new Dimension(CONDITIONAL_NODE_SIZE, CONDITIONAL_NODE_SIZE));
            } else {
                setPreferredSize(new Dimension(NODE_SIZE, NODE_SIZE));
            }
            String description = viewModel.getDescription(node);
            if (StringUtils.isNotBlank(description)){
                setToolTipText(description);
            }
        }

        public Point getHookPoint(NodeView other, boolean isSource){
            Point location = getLocation();
            Point otherLocation = other.getLocation();

            if (node.getType() == GraphModel.NodeType.PROCESS){

                double angle = Math.atan2(otherLocation.y - location.y, otherLocation.x - location.x);

                return new Point(
                        (int)(location.x+NODE_SIZE/2+Math.cos(angle) * NODE_SIZE/2),
                        (int)(location.y+NODE_SIZE/2+Math.sin(angle) * NODE_SIZE/2)
                );
            }
            // else, is conditional, hook points are at center top or center bottom
            if (isSource) {
                return new Point(location.x+getWidth() / 2-1, location.y+getHeight());
            }
            return new Point(location.x+getWidth()/2-1, location.y);
        }

        @Override
        protected void processMouseEvent(MouseEvent e) {
            if (e.getID() == MouseEvent.MOUSE_PRESSED) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    requestFocus();
                    pressPoint = e.getPoint();
                } else if (e.isPopupTrigger() && GraphView.this.editable) {
                    showPopup(e);
                }
            } else if (e.getID() == MouseEvent.MOUSE_CLICKED){
                if (e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1){
                    notifyOpenNode(node);
                }
            } else if (e.getID() == MouseEvent.MOUSE_RELEASED){
                pressPoint = null;
            }
            super.processMouseEvent(e);
        }

        private void showPopup(MouseEvent e){
            JPopupMenu popup = new JPopupMenu();
            for (Action action:GraphView.this.nodeActions){
                popup.add(action);
            }
            popup.add(new SimpleAction("Remove node", this::removeNode));
            popup.show(this,e.getX(),e.getY());
        }

        private void removeNode(ActionEvent e){
            GraphView.this.removeNode(this.node);
        }

        @Override
        protected void processMouseMotionEvent(MouseEvent e) {
            if (e.getID() == MouseEvent.MOUSE_DRAGGED && GraphView.this.editable){
                if (pressPoint != null){
                    setLocation(getX()+ (e.getX() - pressPoint.x), getY() + (e.getY() - pressPoint.y));
                    getParent().repaint();
                }
            }
        }

        protected void paintComponent(Graphics g){

            Graphics2D g2d = (Graphics2D) g;

            int w = getWidth()-4;
            int h = getHeight()-4;

            g.setColor(viewModel.getNodeAttributes(this.node).getColor());

            if (node.getType() == GraphModel.NodeType.PROCESS) {

                g.fillArc(2, 2, w, h, 0, 360);

                g.setColor(Color.BLACK);

                if (this.icon != null) {
                    this.icon.paintIcon(
                        this,
                        g2d,
                        2+(w - this.icon.getIconWidth())/2,
                        2+(h - this.icon.getIconHeight())/2
                    );

                }
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);

                if (isSelected()){
                    ((Graphics2D) g).setStroke(new BasicStroke(3));
                }
                g.drawArc(2, 2, w, h, 0, 360);

            } else {
                int hw = w /2;
                int hh = h /2;
                int[] x = new int[]{0,hw,w,hw,0};
                int[] y = new int[]{hh,0,hh,h,hh};

                g.fillPolygon(x,y,5);

                g.setColor(Color.BLACK);

                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);

                if (isSelected()){
                    ((Graphics2D) g).setStroke(new BasicStroke(3));
                }
                g.drawPolygon(x,y,5);
            }
        }

        private boolean isSelected() {
            return ((GraphView)getParent()).getSelectedNode() == this;
        }
    }

    private void removeNode(GraphModel.Node node) {
        this.model.removeNode(node);
    }

    private void removeEdge(GraphModel.Edge edge) {
        this.model.removeEdge(edge);
    }

    private NodeView getSelectedNode(){
        return selectedNode;
    }

    public String getSelectedNodeName(){
        return selectedNode != null
                ? selectedNode.node.getName()
                : null;
    }

    private abstract static class BaseLabel extends JLabel {

        protected void paintComponent(Graphics g){
            Graphics2D g2d = (Graphics2D) g;
            g2d.setColor(getBackground());
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,0.8f));
            g2d.fillRoundRect(0,0,getWidth()-1,getHeight()-1,10,10);
            g2d.setColor(getBackground().darker());
            g2d.drawRoundRect(0,0,getWidth()-1,getHeight()-1,10,10);
            super.paintComponent(g);
        }
    }

    private class NodeLabel extends BaseLabel {

        private final NodeView node;

        public NodeLabel(NodeView node){
            this.node = node;
            setBorder(JBUI.Borders.empty(5));
            updateView();
            this.node.addComponentListener(new ComponentAdapter() {
                @Override
                public void componentMoved(ComponentEvent e) {
                    moveLabel(e);
                }
            });
            this.addMouseListener(new DoubleClickHandler(this::editNode));
        }

        public void notifyNodeChanged(){
            updateView();
            moveLabel(null);
        }

        public void updateView(){
            if (node.node.getType() == GraphModel.NodeType.CONDITION){
                setText("("+node.node.getName()+") "+ node.node.getCondition());
            } else {
                setText(String.valueOf(node.node.getName()));
            }
            setSize(getPreferredSize());
        }

        private void editNode(MouseEvent e) {
            if (GraphView.this.editable){
                String oldName = node.node.getName();
                InlineEditorPopup.edit(this, new TextFieldInlineEditor(oldName), newName ->{
                    model.renameNode(oldName, newName);
                });
            }
        }

        private void moveLabel(ComponentEvent e) {
            Dimension size = getSize();
            Dimension nodeSize = node.getSize();
            setLocation(node.getX()+nodeSize.width/2 - size.width/2, node.getY()+nodeSize.height);
        }

    }

    private class EdgeLabel extends BaseLabel {
        private final EdgeView edge;

        public EdgeLabel(EdgeView edge){
            this.edge = edge;
            setText(this.edge.edge.getOutcome().toString());
            setBorder(JBUI.Borders.empty(5));
            setSize(getPreferredSize());
            this.edge.addComponentListener(new ComponentAdapter() {
                @Override
                public void componentMoved(ComponentEvent e) {
                    moveLabel(e);
                }

                @Override
                public void componentResized(ComponentEvent e) {
                    moveLabel(e);
                }
            });
        }

        private void moveLabel(ComponentEvent e) {
            Dimension size = getSize();
            Point edgeHook = edge.getEdgeHookPoint();
            setLocation(edgeHook.x - (size.width/2), edgeHook.y - (size.height/2));
        }
    }
}
