package io.datakitchen.ide.editors.graph;

import io.datakitchen.ide.model.Condition;

import java.util.LinkedHashSet;
import java.util.Set;

public class ConditionOutcome {
    private Condition condition;
    private ConditionalOperator operator;
    private String metric;
    private Set<String> targetNodes;
    private String outcomeName;

    public ConditionOutcome(ConditionalOperator operator, String metric, Set<String> targetNodes, String outcomeName) {
        this.operator = operator;
        this.metric = metric;
        this.targetNodes = new LinkedHashSet<>(targetNodes);
        this.outcomeName = outcomeName;
    }

    public boolean sameCondition(ConditionalOperator operator, String metric){
        return this.operator.equals(operator)
                && this.metric.equals(metric);
    }

    public Condition getCondition() {
        return condition;
    }

    public void setCondition(Condition condition) {
        this.condition = condition;
    }

    public ConditionalOperator getOperator() {
        return operator;
    }

    public void setOperator(ConditionalOperator operator) {
        this.operator = operator;
    }

    public String getMetric() {
        return metric;
    }

    public void setMetric(String metric) {
        this.metric = metric;
    }

    public Set<String> getTargetNodes() {
        return targetNodes;
    }

    public void setTargetNodes(Set<String> targetNodes) {
        this.targetNodes = targetNodes;
    }

    public String getOutcomeName() {
        return outcomeName;
    }

    public void setOutcomeName(String outcomeName) {
        this.outcomeName = outcomeName;
    }

    public String toString(){
        return operator.getDescription()+" "+metric;
    }
}
