package io.datakitchen.ide.editors.diff.json;

import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.Consumer;
import io.datakitchen.ide.ui.SimpleAction;
import io.datakitchen.ide.util.JsonUtil;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.*;

public class JsonDiffEditor extends JPanel {

    private final JTree left = new Tree();
    private final JTree right = new Tree();

    private final TreeSelectionListener leftSelectionListener = e -> selectOnRight(e.getPath());
    private final TreeSelectionListener rightSelectionListener = e -> selectOnLeft(e.getPath());

    private final TreeExpansionListener leftExpansionListener = new TreeExpansionListener() {
        @Override
        public void treeExpanded(TreeExpansionEvent event) {
            expandRight(event.getPath());
        }

        @Override
        public void treeCollapsed(TreeExpansionEvent event) {
            collapseRight(event.getPath());
        }
    };

    private final TreeExpansionListener rightExpansionListener = new TreeExpansionListener() {
        @Override
        public void treeExpanded(TreeExpansionEvent event) {
            expandLeft(event.getPath());
        }

        @Override
        public void treeCollapsed(TreeExpansionEvent event) {
            collapseLeft(event.getPath());
        }
    };


    public JsonDiffEditor(){

        JPanel content = new JPanel();
        setLayout(new BorderLayout());
        left.setCellRenderer(new DiffTreeCellRenderer());
        right.setCellRenderer(new DiffTreeCellRenderer());
        JPanel center = new JPanel(null);
        center.setPreferredSize(new Dimension(10,100));
        center.setMaximumSize(new Dimension(10,100));
        JScrollPane leftScroll = new JBScrollPane(left, JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        JPanel leftPanel = new JPanel(new BorderLayout());
        JLabel leftLabel = new JLabel("Local file");
        leftLabel.setPreferredSize(new Dimension(100, 28));
        leftPanel.add(leftLabel, BorderLayout.NORTH);
        leftPanel.add(leftScroll, BorderLayout.CENTER);
        content.add(leftPanel);
        content.add(center);
        JScrollPane rightScroll = new JBScrollPane(right, JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        JPanel rightPanel = new JPanel(new BorderLayout());
        JLabel rightLabel = new JLabel("Remote file");
        rightLabel.setPreferredSize(new Dimension(100,28));
        rightPanel.add(rightLabel, BorderLayout.NORTH);
        rightPanel.add(rightScroll, BorderLayout.CENTER);
        content.add(rightPanel);

        content.setLayout(new DiffLayout(leftPanel, center, rightPanel));

        left.addTreeExpansionListener(leftExpansionListener);
        right.addTreeExpansionListener(rightExpansionListener);
        left.addTreeSelectionListener(leftSelectionListener);
        right.addTreeSelectionListener(rightSelectionListener);
        left.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()){
                    showLeftPopup(e);
                }
            }
        });
        add(new JBScrollPane(content, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.CENTER);

        JPanel legend = new JPanel(new FlowLayout(FlowLayout.LEFT));
        legend.add(new JLabel("Addition", new ColorIcon(new Color(0x88FF88)), JLabel.LEADING));
        legend.add(new JLabel("Replacement",new ColorIcon(new Color(0x8888FF)), JLabel.LEADING));
        legend.add(new JLabel("Substraction",new ColorIcon(new Color(0xFF8888)), JLabel.LEADING));

        JPanel bottomBar = new JPanel(new GridLayout(1,2));
        bottomBar.add(legend);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(new JButton(new SimpleAction("Save", this::saveChanges)));
        buttons.add(new JButton("Reload"));
        bottomBar.add(buttons);

        add(bottomBar, BorderLayout.SOUTH);
    }

    @SuppressWarnings("unchecked")
    private void saveChanges(ActionEvent event) {
        KeyValuePairList<String, Object> root = (KeyValuePairList<String, Object>)left.getModel().getRoot();

        Map<String, Object> document = convert(root);
        String jsonString = JsonUtil.toJsonString(document);
        if (onFinish != null){
            onFinish.consume(jsonString);
        }
    }

    private Consumer<String> onFinish;

    public void onFinish(Consumer<String> onFinish) {
        this.onFinish = onFinish;
    }

    private static class ColorIcon implements Icon {
        private final Color color;
        public ColorIcon(Color color){
            this.color = color;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            g.setColor(color);
            g.fillRect(x,y,13,13);
        }

        @Override
        public int getIconWidth() {
            return 13;
        }

        @Override
        public int getIconHeight() {
            return 13;
        }
    }

    private void selectOnRight(TreePath path) {
        right.removeTreeSelectionListener(rightSelectionListener);
        try {
            right.setSelectionPath(getPathFor(right, left, path));
        }catch(IndexOutOfBoundsException ignored){}
        right.addTreeSelectionListener(rightSelectionListener);
    }

    private void selectOnLeft(TreePath path) {
        left.removeTreeSelectionListener(leftSelectionListener);
        try {
            left.setSelectionPath(getPathFor(left, right, path));
        }catch(IndexOutOfBoundsException ignored){}
        left.addTreeSelectionListener(leftSelectionListener);
    }

    private void expandLeft(TreePath path) {
        left.removeTreeExpansionListener(leftExpansionListener);
        try {
            left.expandPath(getPathFor(left, right, path));
        }catch(IndexOutOfBoundsException ignored){}
        left.addTreeExpansionListener(leftExpansionListener);
    }

    private void collapseLeft(TreePath path) {
        left.removeTreeExpansionListener(leftExpansionListener);
        try {
            left.collapsePath(getPathFor(left, right, path));
        }catch(IndexOutOfBoundsException ignored){}
        left.addTreeExpansionListener(leftExpansionListener);
    }

    private void collapseRight(TreePath path) {
        right.removeTreeExpansionListener(rightExpansionListener);
        try {
            right.collapsePath(getPathFor(right, left, path));
        }catch(IndexOutOfBoundsException ignored){}
        right.addTreeExpansionListener(rightExpansionListener);
    }

    private void expandRight(TreePath path) {
        right.removeTreeExpansionListener(rightExpansionListener);
        try {
            right.expandPath(getPathFor(right, left, path));
        }catch(IndexOutOfBoundsException ignored){}
        right.addTreeExpansionListener(rightExpansionListener);
    }

    private TreePath getPathFor(JTree targetTree, JTree sourceTree, TreePath path) {
        List<Object> otherPath = new ArrayList<>();
        TreeModel targetModel = targetTree.getModel();
        TreeModel sourceModel = sourceTree.getModel();

        Object[] pathArray = path.getPath();

        Object last = targetModel.getRoot();
        Object sourceLast = pathArray[0];
        otherPath.add(last);
        for (int i=1;i<pathArray.length;i++){
            int index = sourceModel.getIndexOfChild(sourceLast, pathArray[i]);
            if (index != -1) {
                Object targetItem = targetModel.getChild(last, index);
                if (targetItem == null){
                    break;
                }
                otherPath.add(targetItem);
                last = targetItem;
                sourceLast = pathArray[i];
            } else {
                break;
            }
        }
        return new TreePath(otherPath.toArray());
    }

    private void showLeftPopup(MouseEvent e) {
        TreePath selection = left.getSelectionPath();
        Patch patch = null;
        if (selection != null && selection.getPath().length > 0){
            Object obj = selection.getLastPathComponent();
            if (obj instanceof Patch){
                patch = (Patch)obj;
            }
        }
        showPopupForPatchValue(e, patch);
    }

    private void showPopupForPatchValue(MouseEvent e, Patch obj) {
        JPopupMenu popup = new JPopupMenu();
        if (obj != null) {
            popup.add(new SimpleAction("Apply remote change", ev -> applyChange(obj)));
            popup.add(new SimpleAction("Ignore remote change", ev -> ignoreChange(obj)));
        }
        popup.add(new SimpleAction("Apply all remote changes", this::applyAll));
        popup.add(new SimpleAction("Ignore all remove changes", this::ignoreAll));
        popup.show(left, e.getX(), e.getY());
    }

    private void applyChange(Patch obj) {
        obj.apply();
        updateDocument();
    }

    private void ignoreChange(Patch obj) {
        obj.ignore();
        updateDocument();
    }

    private void ignoreAll(ActionEvent e) {
        JsonTreeModel model = (JsonTreeModel)left.getModel();
        model.ignoreAll();
        model.updateTree();
        expandAll(left);
    }

    private void applyAll(ActionEvent e) {
        JsonTreeModel model = (JsonTreeModel)left.getModel();
        model.applyAll();
        model.updateTree();
        expandAll(left);
    }

    private void updateDocument(){
        ((JsonTreeModel)left.getModel()).updateTree();
        expandAll(left);
    }

    private void expandAll(JTree tree){
        expand(tree, tree.getModel(), new TreePath(tree.getModel().getRoot()));
    }

    private void expand(JTree tree, TreeModel model, TreePath path) {
        tree.expandPath(path);
        if (!model.isLeaf(path.getLastPathComponent())){
            for (int i=0;i<model.getChildCount(path.getLastPathComponent());i++){
                List<Object> p = new ArrayList<>(Arrays.asList(path.getPath()));
                p.add(model.getChild(path.getLastPathComponent(),i));
                TreePath newPath = new TreePath(p.toArray());
                expand(tree,model,newPath);
            }
        }
    }

    public void setFiles(String left, String right) throws Exception {
        DiffCalculator diffCalculator = new DiffCalculator(left, right);
        List<Map<String, Object>> diff = diffCalculator.getDifference();

        Map<String, Object> leftData = JsonUtil.read(left);
        KeyValuePairList<String, Object> leftParsed = parse(null, leftData);
        putDiff(leftParsed, diff);

        this.left.setModel(new JsonTreeModel(leftParsed));
        this.right.setModel(new JsonTreeModel(parse(null,JsonUtil.read(right))));
        expandAll(this.left);

    }

    @SuppressWarnings({"rawtypes","unchecked"})
    private KeyValuePairList<String, Object> parse(JsonNode parent, Map<String, Object> root) {
        KeyValuePairList<String, Object> result = new KeyValuePairList<>(parent);
        for (Map.Entry<String, Object> entry: root.entrySet()){
            String key = entry.getKey();
            Object value = entry.getValue();
            KeyValuePair pair = new KeyValuePair(parent == null ? result : parent, key, null);
            if (value instanceof Map){
                value = parse(pair,(Map<String, Object>)value);
            } else if (value instanceof List){
                value = parseList(pair, ((List)value));
            } else {
                value = new Value(pair, value);
            }
            pair.setValue(value);
            result.add(pair);
        }
        return result;
    }

    @SuppressWarnings({"rawtypes","unchecked"})
    private JsonNode parseList(JsonNode parent, List value) {
        ListValue list = new ListValue(parent);

        for (Object obj:value){
            if (obj instanceof Map){
                list.add(parse(list, ((Map)obj)));
            } else if (obj instanceof List){
                list.add(parseList(list, ((List)obj)));
            } else {
                list.add(new Value(list, obj));
            }
        }

        return list;
    }

    private void putDiff(KeyValuePairList<String, Object> leftData, List<Map<String, Object>> diff) {
        for (Map<String, Object> operation: diff){
            putOperation(leftData, operation);
        }
    }

    private void putOperation(KeyValuePairList<String, Object> leftData, Map<String, Object> operation) {
        String op = (String)operation.get("op");
        String path = (String)operation.get("path");
        Object value = operation.get("value");

        if (PatchValue.OP_ADD.equals(op)){
            addValue(leftData, path, value);
        } else if (PatchValue.OP_REMOVE.equals(op)){
            removeValue(leftData, path);
        } else if (PatchValue.OP_REPLACE.equals(op)){
            replaceValue(leftData, path, value);
        }

    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> convert(KeyValuePairList<String, Object> root) {
        Map<String, Object> doc = new LinkedHashMap<>();

        for (KeyValuePair<String, Object> kv: root){
            String key = kv.getKey();
            Object value = kv.getValue();

            if (value instanceof KeyValuePairList){
                value = convert(((KeyValuePairList<String, Object>) value));
            } else if (value instanceof ListValue){
                value = convert((ListValue)value);
            } else if (value instanceof Value){
                value = ((Value)value).getValue();
            }

            doc.put(key, value);
        }

        return doc;
    }

    @SuppressWarnings("unchecked")
    private List<Object> convert(ListValue list){
        List<Object> result = new ArrayList<>();

        for (JsonNode o: list){
            Object value = null;
            if (o instanceof KeyValuePairList){
                value = convert((KeyValuePairList<String, Object>) o);
            } else if (o instanceof ListValue){
                value = convert((ListValue) o);
            } else if (o instanceof Value){
                value = ((Value)o).getValue();
            } else if (o instanceof PatchValue){
                value = ((PatchValue)o).getValue();
            }
            result.add(value);
        }

        return result;
    }

    @SuppressWarnings({"rawtypes","unchecked"})
    private void removeValue(KeyValuePairList<String, Object> leftData, String path) {
        JsonNode obj = leftData;
        for (StringTokenizer st = new StringTokenizer(path, "/"); st.hasMoreTokens();){
            String token = st.nextToken();
            boolean lastToken = !st.hasMoreTokens();
            if (lastToken){

                if (StringUtils.isNumeric(token)){
                    int index = Integer.parseInt(token);
                    List list = (List)obj;
                    JsonNode aValue = (JsonNode)list.get(index);
                    list.set(index, new PatchValue(obj, ((Value)aValue).getValue(), PatchValue.OP_REMOVE,null));
                } else {
                    KeyValuePairList map = (KeyValuePairList)obj;
                    KeyValuePair pair = map.getPairByKey(token);
                    map.replace(pair, new PatchKeyValuePair<>(pair.getParent(),pair.getKey(),pair.getValue(), Patch.OP_REMOVE,null));
                }
            } else {
                if (StringUtils.isNumeric(token)) {
                    int index = Integer.parseInt(token);
                    obj = (JsonNode) ((List) obj).get(index);
                } else {
                    obj = (JsonNode) ((KeyValuePairList) obj).getByKey(token);
                }
            }
        }
    }

    @SuppressWarnings({"rawtypes","unchecked"})
    private void replaceValue(KeyValuePairList<String, Object> leftData, String path, Object value) {
        JsonNode obj = leftData;
        for (StringTokenizer st = new StringTokenizer(path, "/"); st.hasMoreTokens();){
            String token = st.nextToken();
            boolean lastToken = !st.hasMoreTokens();
            if (lastToken){

                if (StringUtils.isNumeric(token)){
                    int index = Integer.parseInt(token);
                    List list = (List)obj;
                    Object aValue = list.get(index);
                    list.set(index, new PatchValue(obj, ((Value)aValue).getValue(), PatchValue.OP_REPLACE,value));
                } else {
                    KeyValuePairList map = (KeyValuePairList)obj;
                    KeyValuePair pair = map.getPairByKey(token);
                    map.replace(pair, new PatchKeyValuePair<>(pair.getParent(),pair.getKey(),pair.getValue(), Patch.OP_REPLACE,value));
                }
            } else {
                if (StringUtils.isNumeric(token)) {
                    int index = Integer.parseInt(token);
                    obj = (JsonNode)((List) obj).get(index);
                } else {
                    obj = (JsonNode) ((KeyValuePairList) obj).getByKey(token);
                }
            }
        }
    }

    @SuppressWarnings({"rawtypes","unchecked"})
    private void addValue(KeyValuePairList<String, Object> leftData, String path, Object value) {
        JsonNode obj = leftData;
        for (StringTokenizer st = new StringTokenizer(path, "/"); st.hasMoreTokens();){
            String token = st.nextToken();
            boolean lastToken = !st.hasMoreTokens();
            if (lastToken){

                if (StringUtils.isNumeric(token)){
                    int index = Integer.parseInt(token);
                    ((List)obj).add(index, new PatchValue(obj, ((Value)value).getValue(), PatchValue.OP_ADD,value));
                } else {
                    ((KeyValuePairList)obj).add(new PatchKeyValuePair<>(obj,token,value, Patch.OP_ADD,value));
                }
            } else {
                if (StringUtils.isNumeric(token)) {
                    int index = Integer.parseInt(token);
                    obj = (JsonNode) ((List) obj).get(index);
                } else {
                    obj = (JsonNode) ((KeyValuePairList) obj).getByKey(token);
                }
            }
        }
    }
}
