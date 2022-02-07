package io.datakitchen.ide.tools;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTabbedPane;
import io.datakitchen.ide.editors.graph.*;
import io.datakitchen.ide.util.JsonUtil;
import net.minidev.json.parser.ParseException;
import org.apache.commons.io.IOUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.List;
import java.util.*;

public class RecipeView extends JBTabbedPane implements GraphViewModel {

    private GraphView graphView = new GraphView();

    private static final NodeAttributes SUCCESS_NODE = new NodeAttributes(Color.GREEN.darker());
    private static final NodeAttributes FAILED_NODE = new NodeAttributes(Color.RED.darker());
    private static final NodeAttributes NOT_RUN_NODE = new NodeAttributes(Color.GRAY);
    private final VirtualFile recipeFolder;
    private final Map<String, NodeAttributes> nodeStates = new HashMap<>();
    private final Map<String, Object> recipe;
    private ProgressView progressView = new ProgressView();

    public RecipeView(VirtualFile recipeFolder, File recipeFile) throws IOException, ParseException {
        this.recipeFolder = recipeFolder;
        JTextPane textPane = new JTextPane();
        try (Reader reader = new FileReader(recipeFile)){
            String jsonString = IOUtils.toString(reader);
            recipe = JsonUtil.read(jsonString);
            textPane.setText(jsonString);
        }
        textPane.setFont(new Font("Monospaced",Font.PLAIN,13));

        JScrollPane jsonView = new JBScrollPane(textPane,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        graphView.setRecipeFolder(recipeFolder);
        graphView.setEditable(false);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JBScrollPane(graphView, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED), BorderLayout.CENTER);
        panel.add(progressView, BorderLayout.EAST);
        progressView.setPreferredSize(new Dimension(400,200));
        buildAttributes();
        GraphModel model = buildGraphModel();

        graphView.setModel(model);
        graphView.setViewModel(this);
        graphView.addGraphViewListener(new GraphViewListener() {
            @Override
            public void nodeSelected(GraphViewEvent event) {
                RecipeView.this.nodeSelected(event);
            }
        });
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                graphView.layoutGraph();
            }
        });
        addTab("Recipe",panel);
        addTab("JSON",jsonView);
    }

    private void buildAttributes() {
        Map<String, Object> state = (Map<String, Object>)recipe.get("py/state");
        Map<String, Object> graph = (Map<String, Object>)state.get("_node_dict");
        for (Map.Entry<String, Object> entry:graph.entrySet()){
            String nodeName = entry.getKey();
            Map<String, Object> node = (Map<String, Object>) entry.getValue();
            Map<String, Object> data = (Map<String, Object>) node.get("py/state");
            data = (Map<String, Object>) data.get("progress");
            data = (Map<String, Object>) data.get("py/state");
            data = (Map<String, Object>) data.get("_progress_dict");
            Integer status = (Integer)data.get("node-status");
            if (status != null){
                switch(status){
                    case 17:
                        nodeStates.put(nodeName, SUCCESS_NODE);
                        break;
                    case 18:
                        nodeStates.put(nodeName, FAILED_NODE);
                        break;
                }
            }

        }
    }


    private GraphModel buildGraphModel() {
        Map<String, Object> state = (Map<String, Object>)recipe.get("py/state");
        Map<String, Object> graph = (Map<String, Object>)state.get("_graph");
        Map<String, Object> graphEdges = (Map<String, Object>)graph.get("edge");

        List<List<Object>> edges = new ArrayList<>();

        for (Map.Entry<String, Object> entry: graphEdges.entrySet()){
            String source = entry.getKey();
            Map<String, Object> entryContents = (Map<String, Object>)entry.getValue();

            for (String target:entryContents.keySet()){
                edges.add(Arrays.asList(source,target));
            }

        }
        return GraphModel.fromJson(edges, ConditionsCollection.NULL_COLLECTION);
    }

    @Override
    public NodeAttributes getNodeAttributes(GraphModel.Node node) {
        return nodeStates.getOrDefault(node.getName(), NOT_RUN_NODE);
    }

    void nodeSelected(GraphViewEvent e){
        Map<String, Object> state = (Map<String, Object>)recipe.get("py/state");
        Map<String, Object> graph = (Map<String, Object>)state.get("_node_dict");
        Map<String, Object> nodeData = (Map<String, Object>)graph.get(e.getNode().getName());
        Map<String, Object> data = (Map<String, Object>) nodeData.get("py/state");
        progressView.setProgress((Map<String, Object>) data.get("progress"));
    }
}
