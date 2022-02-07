package io.datakitchen.ide.editors.graph;

import io.datakitchen.ide.model.Condition;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public interface ConditionsCollection {
    /**
     * TODO: This interface should be part of GraphModel
     */
    ConditionsCollection NULL_COLLECTION = new ConditionsCollection() {
        @Override
        public Set<String> getConditionNames() {return new LinkedHashSet<>();}

        @Override
        public Map<String, Object> getCondition(String name) {return null;}

        @Override
        public void setCondition(String name, Map<String, Object> condition) {}

        @Override
        public void nameOutcome(ConditionOutcome outcome) {}

        @Override
        public void removeNodeFromOutcome(ConditionOutcome outcome, String name) {}

        @Override
        public void removeCondition(Condition condition) {}

        @Override
        public void addCondition(String nodeName, Condition condition) {}

        @Override
        public Condition getConditionForNode(String nodeName){
            return null;
        }

        @Override
        public ConditionOutcome getConditionOutcomeForEdge(String sourceNode, String targetNode){
            return null;
        }
    };

    void addCondition(String nodeName, Condition condition);
    Condition getConditionForNode(String nodeName);
    ConditionOutcome getConditionOutcomeForEdge(String sourceNode, String targetNode);
    Set<String> getConditionNames();
    Map<String, Object> getCondition(String name);
    void setCondition(String name, Map<String, Object> condition);

    void nameOutcome(ConditionOutcome outcome);

    void removeNodeFromOutcome(ConditionOutcome outcome, String name);

    void removeCondition(Condition condition);
}
