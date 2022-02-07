package io.datakitchen.ide.model;

import io.datakitchen.ide.editors.graph.ConditionOutcome;
import io.datakitchen.ide.model.MetricConversion;

import java.util.ArrayList;
import java.util.List;

public class Condition {

    public enum Type {
        BINARY,
        SELECT;

        public static Type fromMode(String mode) {
            switch (mode){
                case "condition":
                    return BINARY;
                default:
                    return SELECT;
            }
        }
    }
    private String conditionName;
    private String variable;
    private Type type;
    private MetricConversion conversion;
    private List<ConditionOutcome> outcomes = new ArrayList<>();
    private boolean trueExecution;

    public Condition(){}
    public Condition(String variable, MetricConversion conversion, Type type, boolean trueExecution) {
        this.variable = variable;
        this.conversion = conversion;
        this.type = type;
        this.trueExecution = trueExecution;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String toString(){
        return variable;
    }

    public String getVariable() {
        return variable;
    }

    public void setVariable(String variable) {
        this.variable = variable;
    }

    public MetricConversion getConversion() {
        return conversion;
    }

    public void setConversion(MetricConversion conversion) {
        this.conversion = conversion;
    }

    public List<ConditionOutcome> getOutcomes() {
        return outcomes;
    }

    public void addOutcome(ConditionOutcome outcome){
        outcome.setCondition(this);
        outcomes.add(outcome);
    }

    public String getConditionName() {
        return conditionName;
    }

    public void setConditionName(String conditionName) {
        this.conditionName = conditionName;
    }

    public boolean isTrueExecution() {
        return trueExecution;
    }

    public void setTrueExecution(boolean trueExecution) {
        this.trueExecution = trueExecution;
    }

    public void removeOutcome(ConditionOutcome outcome){
        outcomes.remove(outcome);
    }
}
