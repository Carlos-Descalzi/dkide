package io.datakitchen.ide.tools;

import com.intellij.icons.AllIcons;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTabbedPane;
import com.intellij.ui.treeStructure.Tree;
import io.datakitchen.ide.util.JsonUtil;
import io.datakitchen.ide.ui.FormPanel;
import io.datakitchen.ide.util.ObjectUtil;

import javax.swing.*;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.*;

public class ProgressView extends JBTabbedPane {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");

    private static final int TEST_PASSED = 50;
    private static final int TEST_WARNING = 51;
    private static final int TEST_ERROR = 52;

    private static final String TREE_ROOT = "Tests";
    private static final String NODE_TESTS_NODE = "Node Tests";
    private static final String DATA_SOURCE_TESTS_NODE = "Data Source Tests";
    private static final String DATA_SINK_TESTS_NODE = "Data Sink Tests";
    private static final List<String> ROOT_CHILDREN = Arrays.asList(NODE_TESTS_NODE, DATA_SOURCE_TESTS_NODE, DATA_SINK_TESTS_NODE);

    private Map<String, Object> progress;

    private final JTree tree = new Tree();
    private final JTextPane textPane = new JTextPane();

    private final JLabel nodeStartTime = new JLabel();
    private final JLabel nodeEndTime = new JLabel();
    private final JLabel nodeDuration = new JLabel();
    private final JLabel notebookStartTime = new JLabel();
    private final JLabel notebookEndTime = new JLabel();
    private final JLabel notebookDuration = new JLabel();

    public ProgressView(File progressFile) {
        buildUI();
        try (InputStream input = new FileInputStream(progressFile)){
            setProgress(JsonUtil.read(input));
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }
    public ProgressView(){
        this((Map<String, Object>) null);
    }
    public ProgressView(Map<String, Object> progress){
        buildUI();
        setProgress(progress);
    }

    private void buildUI(){
        addTab("Tests",buildTestsView());
        addTab("Timings", buildTimingsView());
        addTab("JSON",buildProgressView());
    }

    public void setProgress(Map<String, Object> progress){
        this.progress = progress;
        tree.setModel(buildModel());
        tree.expandPath(new TreePath(new Object[]{TREE_ROOT, NODE_TESTS_NODE}));
        tree.expandPath(new TreePath(new Object[]{TREE_ROOT, DATA_SOURCE_TESTS_NODE}));
        tree.expandPath(new TreePath(new Object[]{TREE_ROOT, DATA_SINK_TESTS_NODE}));
        if (progress != null) {
            String progressString = JsonUtil.toJsonString(progress);
            textPane.setText(progressString);
            loadTimings();
        } else {
            textPane.setText("");
            clearTimings();
        }

    }

    private void loadTimings() {
        Map<String, Object> state = ObjectUtil.cast(progress.get("py/state"));
        Map<String, Object> progressDict = ObjectUtil.cast(state.get("_progress_dict"));
        Map<String, Object> timingData = ObjectUtil.cast(progressDict.get("timing-data"));

        Long nodeStartTime = (Long)timingData.get("node-start-time");
        Long nodeEndTime = (Long)timingData.get("node-end-time");
        Long notebookStartTime = (Long)timingData.get("node-start-time");
        Long notebookEndTime = (Long)timingData.get("notebook-end-time");

        if (nodeStartTime != null) {
            this.nodeStartTime.setText(formatDate(nodeStartTime));
            this.nodeEndTime.setText(formatDate(nodeEndTime));
            this.nodeDuration.setText(formatDuration(nodeEndTime,nodeStartTime));
            this.notebookStartTime.setText(formatDate(notebookStartTime));
            this.notebookEndTime.setText(formatDate(notebookEndTime));
            this.notebookDuration.setText(formatDuration(notebookEndTime,notebookStartTime));
        } else {
            clearTimings();
        }
    }

    private String formatDate(Long date){
        if (date == null){
            return "";
        }
        return DATE_FORMAT.format(new Date(date));
    }

    private String formatDuration(Long end, Long start) {
        if (end == null || start == null){
            return "";
        }
        long duration = end - start;
        long millis = duration % 1000;
        long seconds = duration / 1000;
        return String.format("%d:%02d:%02d.%03d", (seconds / 3600), (seconds % 3600) / 60, (seconds % 60), millis);
    }

    private void clearTimings(){
        this.nodeStartTime.setText("");
        this.nodeEndTime.setText("");
        this.notebookStartTime.setText("");
        this.notebookEndTime.setText("");
        this.nodeDuration.setText("");
        this.notebookDuration.setText("");
    }

    private JComponent buildTestsView(){
        tree.setModel(buildModel());
        tree.expandPath(new TreePath(new Object[]{TREE_ROOT, NODE_TESTS_NODE}));
        tree.expandPath(new TreePath(new Object[]{TREE_ROOT, DATA_SOURCE_TESTS_NODE}));
        tree.expandPath(new TreePath(new Object[]{TREE_ROOT, DATA_SINK_TESTS_NODE}));
        tree.setRootVisible(false);
        tree.setCellRenderer(new DefaultTreeCellRenderer(){
            @Override
            public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {

                JLabel c = (JLabel)super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
                if (value instanceof Map.Entry){
                    Map.Entry<String, Map<String, Object>> entry = ObjectUtil.cast(value);
                    Map<String, Object> test = entry.getValue();
                    String testLogic = (String)test.get("test-logic-used");

                    c.setText(entry.getKey()+ " ("+testLogic+")");

                    Integer testResult = (Integer) test.get("test-result");
                    switch (testResult){
                        case TEST_PASSED:
                            c.setIcon(AllIcons.General.InspectionsOK);
                            break;
                        case TEST_WARNING:
                            c.setIcon(AllIcons.General.Warning);
                            break;
                        case TEST_ERROR:
                            c.setIcon(AllIcons.General.Error);
                            break;
                    }
                } else {
                    c.setIcon(null);
                }
                return c;
            }
        });

        return new JBScrollPane(tree,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    }

    private JComponent buildTimingsView() {
        FormPanel panel = new FormPanel();

        panel.addField("Node start time", nodeStartTime);
        panel.addField("Node end time", nodeEndTime);
        panel.addField("Node duration", nodeDuration);
        panel.addField("Notebook start time", notebookStartTime);
        panel.addField("Notebook end time", notebookEndTime);
        panel.addField("Notebook duration", notebookDuration);

        return panel;
    }


    private JComponent buildProgressView() {
        textPane.setFont(new Font("Monospaced",Font.PLAIN,13));

        return new JBScrollPane(textPane, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    }

    private TreeModel buildModel(){

        if (progress != null) {
            Map<String, Object> testsData = (Map<String, Object>) ((Map<String, Object>) ((Map<String, Object>) progress.get("py/state")).get("_progress_dict")).get("test-data");

            if (!testsData.isEmpty()) {
                testsData = ObjectUtil.cast(testsData.values().iterator().next());
            }
            if (!testsData.isEmpty()) {
                testsData = ObjectUtil.cast(testsData.values().iterator().next());
            }

            Map<String, Object> nodeTests = ObjectUtil.cast(testsData.get("node-test"));
            Map<String, Object> dataSourceTests = ObjectUtil.cast(testsData.get("datasource-test"));
            Map<String, Object> dataSinkTests = ObjectUtil.cast(testsData.get("datasink-test"));

            List<Map.Entry<String, Object>> nodeTestsEntries = nodeTests != null ? new ArrayList<>(nodeTests.entrySet()) : new ArrayList<>();
            List<Map.Entry<String, Object>> dataSourceTestsEntries = dataSourceTests != null ? new ArrayList<>(dataSourceTests.entrySet()) : new ArrayList<>();
            List<Map.Entry<String, Object>> dataSinkTestsEntries = dataSinkTests != null ? new ArrayList<>(dataSinkTests.entrySet()) : new ArrayList<>();

            return new TreeModel() {

                @Override
                public Object getRoot() {
                    return TREE_ROOT;
                }

                @Override
                public Object getChild(Object parent, int index) {
                    if (parent == TREE_ROOT) {
                        return ROOT_CHILDREN.get(index);
                    } else if (parent == NODE_TESTS_NODE) {
                        return nodeTestsEntries.get(index);
                    } else if (parent == DATA_SOURCE_TESTS_NODE) {
                        return dataSourceTestsEntries.get(index);
                    } else if (parent == DATA_SINK_TESTS_NODE) {
                        return dataSinkTestsEntries.get(index);
                    }
                    return null;
                }

                @Override
                public int getChildCount(Object parent) {
                    if (parent == TREE_ROOT) {
                        return ROOT_CHILDREN.size();
                    } else if (parent == NODE_TESTS_NODE) {
                        return nodeTestsEntries.size();
                    } else if (parent == DATA_SOURCE_TESTS_NODE) {
                        return dataSourceTestsEntries.size();
                    } else if (parent == DATA_SINK_TESTS_NODE) {
                        return dataSinkTestsEntries.size();
                    }
                    return 0;
                }

                @Override
                public boolean isLeaf(Object node) {
                    return node instanceof Map.Entry;
                }

                @Override
                public void valueForPathChanged(TreePath path, Object newValue) {

                }

                @Override
                public int getIndexOfChild(Object parent, Object child) {
                    if (parent == TREE_ROOT) {
                        return ROOT_CHILDREN.indexOf(child);
                    } else if (parent == NODE_TESTS_NODE) {
                        return nodeTestsEntries.indexOf(child);
                    } else if (parent == DATA_SOURCE_TESTS_NODE) {
                        return dataSourceTestsEntries.indexOf(child);
                    } else if (parent == DATA_SINK_TESTS_NODE) {
                        return dataSinkTestsEntries.indexOf(child);
                    }
                    return 0;
                }

                @Override
                public void addTreeModelListener(TreeModelListener l) {
                }

                @Override
                public void removeTreeModelListener(TreeModelListener l) {
                }
            };
        } else {
            return new TreeModel() {
                @Override
                public Object getRoot() {
                    return ROOT_CHILDREN;
                }

                @Override
                public Object getChild(Object parent, int index) {
                    return null;
                }

                @Override
                public int getChildCount(Object parent) {
                    return 0;
                }

                @Override
                public boolean isLeaf(Object node) {
                    return true;
                }

                @Override
                public void valueForPathChanged(TreePath path, Object newValue) {

                }

                @Override
                public int getIndexOfChild(Object parent, Object child) {
                    return 0;
                }

                @Override
                public void addTreeModelListener(TreeModelListener l) {

                }

                @Override
                public void removeTreeModelListener(TreeModelListener l) {

                }
            };
        }
    }

}
