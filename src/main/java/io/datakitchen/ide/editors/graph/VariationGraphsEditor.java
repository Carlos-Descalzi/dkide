package io.datakitchen.ide.editors.graph;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.VirtualFile;
import io.datakitchen.ide.editors.*;
import io.datakitchen.ide.model.Condition;
import io.datakitchen.ide.model.MetricConversion;
import io.datakitchen.ide.ui.*;
import io.datakitchen.ide.util.ObjectUtil;
import io.datakitchen.ide.util.VariationUtil;


import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

public class VariationGraphsEditor extends JPanel implements ConditionsCollection, Disposable, VariationItemEditor {

    private static final Logger LOGGER = Logger.getInstance(VariationsFileEditor.class);

    private final EventSupport<VariationEditionListener> editionListeners = EventSupport.of(VariationEditionListener.class);
    private final EventSupport<DocumentChangeListener> eventSupport = EventSupport.of(DocumentChangeListener.class);
    private final ItemListListener itemListListener = new ItemListListener() {
        @Override
        public void itemAdded(ItemListEvent event) {
            notifyItemAdded(event);
        }

        @Override
        public void itemRemoved(ItemListEvent event) {
            notifyItemRemoved(event);
        }

        @Override
        public void itemChanged(ItemListEvent event) {
            notifyItemChanged(event);
        }
    };

    private final ItemList<VariationGraph> graphs = new ItemList<>(this::createGraph);
    private final GraphEditor graphEditor;
    private final DocumentChangeListener graphEditorListener = this::graphChanged;
    private final ListSelectionListener listListener = this::selectGraph;
    private final Map<String, Condition> conditionsByNode = new LinkedHashMap<>();
    private final VirtualFile recipeFolder;
    private final Project project;
    private Map<String, Object> variationDocument;
    private VariationGraph currentGraph;

    public VariationGraphsEditor(Project project, VirtualFile recipeFolder){
        this.recipeFolder = recipeFolder;
        this.project = project;
        this.graphEditor = new GraphEditor(project, recipeFolder);
        setLayout(new BorderLayout());


        add(graphs,BorderLayout.WEST);
        Disposer.register(this, graphs);
        Disposer.register(this, graphEditor);
        add(graphEditor, BorderLayout.CENTER);
        graphs.setSupportedFlavor(GraphInfo.FLAVOR);
        graphs.setPasteHandler(this::handlePastGraph);

        updateActions();
        enableEvents();
    }

    private VariationGraph createGraph(){
        return new VariationGraph("graph-"+(graphs.getDataSize()+1));
    }

    @Override
    public void dispose() {

    }

    private void enableEvents(){
        graphs.addListSelectionListener(listListener);
        graphEditor.addDocumentChangeListener(graphEditorListener);
        graphs.addDocumentChangeListener(graphEditorListener);
        graphs.addItemListListener(itemListListener);
    }

    private void disableEvents(){
        graphs.removeListSelectionListener(listListener);
        graphEditor.removeDocumentChangeListener(graphEditorListener);
        graphs.removeDocumentChangeListener(graphEditorListener);
        graphs.addItemListListener(itemListListener);
    }

    public void addDocumentChangeListener(DocumentChangeListener listener){
        this.eventSupport.addListener(listener);
    }
    public void removeDocumentChangeListener(DocumentChangeListener listener){
        this.eventSupport.removeListener(listener);
    }

    public void setVariationDocument(Map<String, Object> variationDocument) {
        disableEvents();
        this.variationDocument = variationDocument;
        List<VariationGraph> graphList = new ArrayList<>();
        if (variationDocument != null){
            Map<String,Object> graphsObject = ObjectUtil.cast(variationDocument.get("graph-setting-list"));

            if (graphsObject != null) {
                graphList.addAll(
                    graphsObject.entrySet().stream().map(
                        (Map.Entry<String, Object> e)->VariationGraph.fromEntry(recipeFolder.getName(), e, this)
                    ).collect(Collectors.toList()));
            }
        }
        loadConditions();
        this.graphs.setData(graphList);
        currentGraph = null;
        graphEditor.loadGraph(null);
        updateActions();
        enableEvents();
        if (graphList.size() > 0){
            this.graphs.setSelectedIndex(0);
        }
    }

    private void selectGraph(ListSelectionEvent e){
        disableEvents();
        if (currentGraph != null){
            graphEditor.saveGraph(currentGraph);
        }

        currentGraph = graphs.getSelected();

        graphEditor.loadGraph(currentGraph);
        enableEvents();
        updateActions();
    }

    private void graphChanged(DocumentChangeEvent documentChangeEvent) {
        saveDocument();
        eventSupport.getProxy().documentChanged(new DocumentChangeEvent(this));
        graphs.repaint();
    }

    private void updateActions(){
        graphEditor.setEnabled(currentGraph != null);
    }

    public void saveDocument() {
        saveConditions();
        Map<String, Object> graphs = new LinkedHashMap<>();
        if (currentGraph != null){
            graphEditor.saveGraph(currentGraph);
        }

        for (VariationGraph item:this.graphs.getData()){
            graphs.put(item.getName(),item.getGraph());
        }

        variationDocument.put("graph-setting-list", graphs);
    }

    public void notifyNodeRenamed(String oldValue, String newValue) {
        for (VariationGraph graph:graphs.getData()){
            graph.renameNode(oldValue, newValue);
            if (graph == currentGraph){
                graphEditor.loadGraph(currentGraph);
            }
        }
    }

    public void notifyNodeRemoved(String name) {
        for (VariationGraph graph:graphs.getData()){
            graph.removeNode(name);
            if (graph == currentGraph){
                graphEditor.loadGraph(currentGraph);
            }
        }
    }

    private void saveConditions(){
        Map<String, Object> conditionsList = new LinkedHashMap<>();
        variationDocument.put("conditions-list", conditionsList);
        if (conditionsByNode.isEmpty()){
            return;
        }

        for (Map.Entry<String, Condition> entry: conditionsByNode.entrySet()){
            String nodeName = entry.getKey();
            Condition condition = entry.getValue();

            List<ConditionOutcome> outcomes = condition.getOutcomes();
            for (ConditionOutcome outcome : outcomes) {
                Map<String, Object> conditionMap = new LinkedHashMap<>();
                conditionMap.put("node", nodeName);
                conditionMap.put("mode", condition.getType() == Condition.Type.BINARY ? "condition" : "variable");
                conditionMap.put("index", 1);

                Map<String, Object> conditionBody = new LinkedHashMap<>();
                conditionBody.put("metric", outcome.getMetric());
                conditionBody.put("variable", condition.getVariable());
                conditionBody.put("compare", outcome.getOperator().getDefinition());
                conditionBody.put("type", condition.getConversion().getDefinition());
                conditionBody.put("isTrueExecution", condition.isTrueExecution());
                conditionBody.put("execute", new ArrayList<>(outcome.getTargetNodes()));

                Map<String, Object> c = new LinkedHashMap<>();
                c.put("list", new ArrayList<>(List.of(conditionBody)));
                conditionMap.put("condition", c);

                conditionsList.put(outcome.getOutcomeName(), conditionMap);
            }
        }
    }

    private void loadConditions(){
        this.conditionsByNode.clear();
        Map<String, Object> conditions = ObjectUtil.cast(variationDocument.get("conditions-list"));
        if (conditions == null){
            return;
        }

        Map<String, List<Map<String, Object>>> conditionsByNode = new LinkedHashMap<>();

        for (Map.Entry<String, Object> entry: conditions.entrySet()){
            String outcomeName = entry.getKey();
            Map<String, Object> conditionJson = ObjectUtil.cast(entry.getValue());

            String mode = (String)conditionJson.get("mode");
            String node = (String)conditionJson.get("node");

            Map<String, Object> data = ObjectUtil.cast(conditionJson.get("condition"));
            List<Map<String, Object>> array = ObjectUtil.cast(data.get("list"));

            Map<String, Object> condition = array.get(0);
            condition.put("mode",mode);
            condition.put("name",outcomeName);

            conditionsByNode
                .computeIfAbsent(node, (String s)->new ArrayList<>())
                .add(condition);
        }

        for (Map.Entry<String, List<Map<String, Object>>> entry: conditionsByNode.entrySet()){
            String nodeName = entry.getKey();
            List<Map<String, Object>> conditionList = entry.getValue();

            Map<String, Object> firstCondition = conditionList.get(0);

            String mode = (String)firstCondition.get("mode");
            String variable = (String)firstCondition.get("variable");
            String conversion = (String)firstCondition.get("type");
            Boolean trueExecution = (Boolean)firstCondition.getOrDefault("isTrueExecution",false);

            Condition condition = new Condition(
                    variable,
                    MetricConversion.fromDefinition(conversion),
                    Condition.Type.fromMode(mode),
                    trueExecution
            );
            condition.setConditionName(nodeName);

            for (Map<String, Object> item: conditionList){

                String outcomeName = (String)item.get("name");
                String compare = (String)item.get("compare");
                String metric = (String)item.get("metric");

                Set<String> targetNodes = new LinkedHashSet<>(ObjectUtil.cast(item.get("execute")));

                condition.addOutcome(new ConditionOutcome(
                        ConditionalOperator.fromDefinition(compare),
                        metric,
                        targetNodes,
                        outcomeName
                ));
            }

            this.conditionsByNode.put(nodeName, condition);
        }
    }

    @Override
    public void addCondition(String nodeName, Condition condition) {
        conditionsByNode.put(nodeName, condition);
    }

    @Override
    public Condition getConditionForNode(String nodeName) {
        return conditionsByNode.get(nodeName);
    }

    public ConditionOutcome getConditionOutcomeForEdge(String sourceNode, String targetNode){
        Condition condition = conditionsByNode.get(sourceNode);
        if (condition != null){
            for (ConditionOutcome outcome: condition.getOutcomes()){
                if (outcome.getTargetNodes().contains(targetNode)){
                    return outcome;
                }
            }
        }
        return null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Set<String> getConditionNames() {
        Map<String, Object> conditions = (Map<String, Object>)variationDocument.get("conditions-list");
        return conditions == null ? new LinkedHashSet<>() : conditions.keySet();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> getCondition(String name) {
        Map<String, Object> conditions = (Map<String, Object>)variationDocument.get("conditions-list");
        return conditions == null ? null : (Map<String, Object>)conditions.get(name);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void setCondition(String name, Map<String, Object> condition) {
        Map<String, Object> conditions = (Map<String, Object>) variationDocument.computeIfAbsent(
            "conditions-list",
            (String key)->new LinkedHashMap<>()
        );
        conditions.put(name, condition);
    }

    @Override
    public void nameOutcome(ConditionOutcome outcome) {
        int n = 1;

        String prefix = outcome.getCondition().getConditionName()+"_";

        while(outcomeNodeExists(prefix+n)){
            n++;
        }
        outcome.setOutcomeName(prefix+n);
    }

    @Override
    public void removeNodeFromOutcome(ConditionOutcome outcome, String name) {
        outcome.getTargetNodes().remove(name);
        if (outcome.getTargetNodes().isEmpty()){
            Condition condition = outcome.getCondition();
            condition.removeOutcome(outcome);
        }
    }

    @Override
    public void removeCondition(Condition condition) {
        conditionsByNode.remove(condition.getConditionName());
    }

    private boolean outcomeNodeExists(String name) {
        for (Condition c:conditionsByNode.values()){
            for (ConditionOutcome o:c.getOutcomes()){
                if (name.equals(o.getOutcomeName())){
                    return true;
                }
            }
        }
        return false;
    }

    public VariationGraph getGraphByName(String graphName) {
        return graphs.getData()
            .stream().filter(g -> g.getName().equals(graphName))
            .findFirst()
            .orElse(null);
    }


    private void handlePastGraph(Transferable transferable) {
        try {
            Module module = ModuleUtil.findModuleForFile(this.recipeFolder, this.project);
            if (module != null) {
                GraphInfo info = (GraphInfo) transferable.getTransferData(GraphInfo.FLAVOR);
                ApplicationManager.getApplication().runWriteAction(() ->
                    VariationUtil.copyNodes(info.getRecipeName(), info.getGraph(), module)
                );
                graphs.addItem(info.getGraph());
            }
        }catch(UnsupportedFlavorException | IOException ex){
            LOGGER.error(ex);
        }
    }

    @Override
    public void addVariationEditionListener(VariationEditionListener listener) {
        editionListeners.addListener(listener);
    }

    @Override
    public void removeVariationEditionListener(VariationEditionListener listener) {
        editionListeners.removeListener(listener);
    }
    private void notifyItemAdded(ItemListEvent event){
        editionListeners.getProxy().variationItemAdded(
                new VariationEditionEvent(this,"graph",event.getItem().getName())
        );
    }
    private void notifyItemRemoved(ItemListEvent event){
        editionListeners.getProxy().variationItemRemoved(
                new VariationEditionEvent(this,"graph",event.getItem().getName())
        );
    }
    private void notifyItemChanged(ItemListEvent event){
        editionListeners.getProxy().variationItemChanged(
                new VariationEditionEvent(this,"graph",event.getItem().getName(), event.getOldName())
        );
    }
}
