package io.datakitchen.ide.editors.graph;

import com.google.common.graph.MutableNetwork;
import com.google.common.graph.NetworkBuilder;
import io.datakitchen.ide.model.Condition;
import io.datakitchen.ide.ui.EventSupport;

import java.util.*;

public class GraphModel {

    public enum NodeType {
        PROCESS,
        CONDITION;
    }

    private final EventSupport<GraphModelListener> listeners = EventSupport.of(GraphModelListener.class);
    private final MutableNetwork<Node, Edge> graph = NetworkBuilder.directed().build();
    private final ConditionsCollection conditionsCollection;

    public GraphModel(ConditionsCollection conditionsCollection){
        this.conditionsCollection = conditionsCollection;
    }

    public GraphModel(Set<Node> nodes, Set<Edge> edges, ConditionsCollection conditionsCollection){
        this.conditionsCollection = conditionsCollection;
        for (Node node:nodes){
            this.graph.addNode(node);
        }
        for (Edge edge: edges){
            this.graph.addEdge(edge.from,edge.to,edge);
        }
    }

    public static GraphModel fromJson(List<List<Object>> graphArray, ConditionsCollection conditionsCollection){
        GraphModel model = new GraphModel(conditionsCollection);
        for (int i =0;i<graphArray.size();i++){
            List<Object> item = graphArray.get(i);

            if (item.size() == 3){
                // has conditions
                Map<String, Object> nodeData = (Map<String, Object>)item.get(2);

                String sourceNodeName = (String)item.get(0);
                String targetNodeName = (String)item.get(1);

                Condition condition = conditionsCollection.getConditionForNode(sourceNodeName);
                ConditionOutcome outcome = conditionsCollection.getConditionOutcomeForEdge(sourceNodeName, targetNodeName);

                Node node = model.graph.nodes().stream().filter((Node n)->n.getName().equals(sourceNodeName)).findFirst().orElse(null);

                if (node != null){
                    if (node.getType() != NodeType.CONDITION){
                        node.setType(NodeType.CONDITION);
                        node.setCondition(condition);
                    }
                } else {
                    node = Node.condition(sourceNodeName, condition);
                }

                Edge edgeObj = new Edge(
                    node,
                    Node.process(targetNodeName),
                    outcome
                );
                model.graph.addEdge(edgeObj.from,edgeObj.to,edgeObj);
            } else if (item.size() == 2){
                // edge
                Edge edgeObj = new Edge(Node.process((String)item.get(0)),Node.process((String)item.get(1)));
                model.graph.addEdge(edgeObj.from,edgeObj.to,edgeObj);
            } else if (item.size() == 1){
                // single node
                model.graph.addNode(Node.process((String)item.get(0)));
            }
        }
        return model;

    }

    public void addGraphModelListener(GraphModelListener listener){
        this.listeners.addListener(listener);
    }

    public void removeGraphModelListener(GraphModelListener listener){
        this.listeners.removeListener(listener);
    }

    public void addNode(Node node){
        if (this.graph.addNode(node)){
            if (node.getCondition() != null){
                conditionsCollection.addCondition(node.getName(),node.getCondition());
            }
            listeners.getProxy().nodeAdded(new GraphModelEvent(this, node));
        }
    }

    public void removeNode(Node node){
        Set<Edge> edgesToRemove = new HashSet<>(this.graph.outEdges(node));
        edgesToRemove.addAll(this.graph.inEdges(node));

        if (this.graph.removeNode(node)){
            for (Edge edge: edgesToRemove){
                listeners.getProxy().edgeRemoved(new GraphModelEvent(this, edge));
            }
            listeners.getProxy().nodeRemoved(new GraphModelEvent(this, node));
        }
        if (node.getCondition() != null){
            conditionsCollection.removeCondition(node.getCondition());
        }
    }

    public void addEdge(Node from, Node to, ConditionOutcome outcome) {
        if (outcome != null) {
            conditionsCollection.nameOutcome(outcome);
        }
        Edge edge = new Edge(from, to, outcome);
        if (this.graph.addEdge(from, to, edge)){
            listeners.getProxy().edgeAdded(new GraphModelEvent(this, edge));
        }
    }

    public void addEdge(Node from, Node to){
        addEdge(from, to, null);
    }

    public void removeEdge(Node from, Node to){
        removeEdge(new Edge(from, to));
    }

    public void removeEdge(Edge edge){
        if (this.graph.removeEdge(edge)) {
            if (edge.getOutcome() != null){
                conditionsCollection.removeNodeFromOutcome(edge.getOutcome(),edge.to.name);
            }
            listeners.getProxy().edgeRemoved(new GraphModelEvent(this, edge));
        }
    }

    public Set<Node> getNodes() {
        return this.graph.nodes();
    }

    public Set<Edge> getEdges() {
        return this.graph.edges();
    }

    public Node getNodeByName(String name) {
        return getNodes().stream().filter(n -> n.getName().equals(name)).findFirst().orElse(null);
    }

    public void renameNode(String oldName, String newName) {
        Node node = getNodeByName(oldName);
        if (node != null){
            node.setName(newName);
            listeners.getProxy().nodeChanged(new GraphModelEvent(this, node, oldName, newName));
        }
    }

    public List<Node> roots() {
        List<Node> roots = new ArrayList<>();
        for (Node node :graph.nodes()){
            if (graph.inDegree(node) == 0){
                roots.add(node);
            }
        }
        return roots;
    }

    public Set<Node> successors(Node node){
        return this.graph.successors(node);
    }

    public List<List<Object>> toJSONArray() {

        List<List<Object>> array = new ArrayList<>();
        Set<Node> usedNodes = new HashSet<>();

        for (Edge edge:graph.edges()){

            List<Object> edgeArray = new ArrayList<>();
            edgeArray.add(edge.getFrom().name);
            edgeArray.add(edge.getTo().name);

            if (edge.getOutcome() != null){
                Map<String, Object> conditionName = new LinkedHashMap<>();
                conditionName.put("condition", edge.getOutcome().getOutcomeName());
                edgeArray.add(conditionName);
            }
            array.add(edgeArray);
            usedNodes.addAll(edge.nodes());
        }

        Set<Node> nodes = new HashSet<>(graph.nodes());
        nodes.removeAll(usedNodes);
        for (Node node:nodes){
            List<Object> edgeArray = new ArrayList<>();
            edgeArray.add(node.name);
            array.add(edgeArray);
        }

        return array;
    }

    public static class Node {
        private String name;
        private NodeType type;
        private Condition condition;

        public static Node process(String name){
            return new Node(name,NodeType.PROCESS, null);
        }

        public static Node condition(String name, Condition condition){
            return new Node(name, NodeType.CONDITION, condition);
        }

        private Node(String name, NodeType type, Condition condition){
            this.name = name;
            this.type = type;
            this.condition = condition;
        }

        public void setCondition(Condition condition){
            this.condition = condition;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setType(NodeType type) {
            this.type = type;
        }

        public String getName() {
            return name;
        }

        public NodeType getType() {
            return type;
        }

        public Condition getCondition() {
            return condition;
        }

        public boolean equals(Object other){
            return other instanceof Node
                && name.equals(((Node)other).name);
        }

        public int hashCode(){
            return name.hashCode();
        }
    }

    public static class Edge {
        private final Node from;
        private final Node to;
        private ConditionOutcome outcome;

        public Edge(Node from, Node to){
            this.from = from;
            this.to = to;
        }
        public Edge(Node from, Node to, ConditionOutcome outcome){
            this.from = from;
            this.to = to;
            this.outcome = outcome;
        }

        public ConditionOutcome getOutcome() {
            return outcome;
        }

        public boolean equals(Object other){
            return other instanceof Edge
                    && from.equals(((Edge)other).from)
                    && to.equals(((Edge)other).to);
        }

        public List<Node> nodes(){
            return Arrays.asList(from, to);
        }

        public int hashCode(){
            return (from.name + to.name).hashCode();
        }

        public boolean contains(Node node){
            return from.equals(node) || to.equals(node);
        }

        public Node getFrom() {
            return from;
        }

        public Node getTo() {
            return to;
        }
    }

}
