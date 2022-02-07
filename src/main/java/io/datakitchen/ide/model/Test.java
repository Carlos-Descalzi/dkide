package io.datakitchen.ide.model;

import io.datakitchen.ide.ui.EventSupport;
import io.datakitchen.ide.util.ObjectUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.LinkedHashMap;
import java.util.Map;

public class Test {
    private final EventSupport<PropertyChangeListener> listeners = EventSupport.of(PropertyChangeListener.class);
    private RuntimeVariable variable;
    private TestOperator operator;
    private String metricString;
    private TestAction testAction;
    private MetricConversion type;
    private String description;

    public Test() {
    }

    public Test(RuntimeVariable variable, TestOperator operator, String metricString, TestAction testAction) {
        this(variable, operator, metricString, testAction, null);
    }

    public Test(RuntimeVariable variable, TestOperator operator, String metricString, TestAction testAction, MetricConversion type) {
        this(variable, operator, metricString, testAction, type, null);
    }

    public Test(RuntimeVariable variable, TestOperator operator, String metricString, TestAction testAction, MetricConversion type, String description) {
        this.variable = variable;
        this.operator = operator;
        this.metricString = metricString;
        this.testAction = testAction;
        this.type = type != null ? type : (variable.getAttribute() != null ? variable.getAttribute().getType() : null);
        this.description = description;
    }

    public RuntimeVariable getVariable() {
        return variable;
    }

    public void setVariable(RuntimeVariable variable) {
        RuntimeVariable oldVariable = this.variable;
        this.variable = variable;
        firePropertyChange("variable", oldVariable, variable);
    }

    public TestOperator getOperator() {
        return operator;
    }

    public void setOperator(TestOperator operator) {
        TestOperator oldOperator = this.operator;
        this.operator = operator;
        firePropertyChange("operator", oldOperator, operator);
    }

    public String getMetricString() {
        return metricString;
    }

    public void setMetricString(String metricString) {
        String oldMetricString = this.metricString;
        this.metricString = metricString;
        firePropertyChange("metricString", oldMetricString, metricString);
    }

    public TestAction getTestAction() {
        return testAction;
    }

    public void setTestAction(TestAction testAction) {
        TestAction oldTestAction = this.testAction;
        this.testAction = testAction;
        firePropertyChange("testAction", oldTestAction, testAction);
    }

    public MetricConversion getType() {
        return type;
    }

    public void setType(MetricConversion type) {
        MetricConversion oldType = this.type;
        this.type = type;
        firePropertyChange("type",oldType, type);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener){
        this.listeners.addListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener){
        this.listeners.removeListener(listener);
    }

    protected void firePropertyChange(String propertyName, Object oldValue, Object newValue){
        this.listeners.getProxy().propertyChange(new PropertyChangeEvent(this, propertyName, oldValue, newValue));
    }

    public int hashCode(){
        return new HashCodeBuilder()
                .append(operator)
                .append(metricString)
                .append(testAction)
                .append(type)
                .append(variable != null ? variable.getAttribute() : null)
                .toHashCode();
    }

    public boolean equals(Object other){
        if (!(other instanceof Test)){
            return false;
        }
        EqualsBuilder builder = new EqualsBuilder()
                .append(operator, ((Test) other).operator)
                .append(metricString,((Test) other).metricString)
                .append(testAction, ((Test) other).testAction)
                .append(type, ((Test) other).type);

        if (variable != null && ((Test)other).variable != null) {
            builder.append(variable.getAttribute(), ((Test) other).variable.getAttribute());
        }
        return builder.isEquals();
    }

    public String toString(){
        String string = variable.getAttribute() == null
                ? variable.getVariableName()
                : variable.getAttribute().getDisplayName();
        if (type != null){
            string +=" (as "+type.getDescription()+")";
        }
        string += " "+operator.getDisplayName()+" "+metricString;

        return string;
    }

    public Map<String, Object> toJson() {
        RuntimeVariable variable = getVariable();
        MetricConversion type = getType();
        if (type == null){
            type = variable.getAttribute().getType();
        }
        Map<String, Object> json = new LinkedHashMap<>();
        json.put("test-variable", variable.getVariableName());
        json.put("action",getTestAction().getDefinition());
        json.put("type",type.getDefinition());

        if (StringUtils.isNotBlank(description)){
            json.put("test-documentation", description);
        }

        Map<String, Object> logic = new LinkedHashMap<>();
        logic.put("test-compare", getOperator().getName());
        logic.put("test-metric", getMetricString());
        json.put("test-logic", logic);
        return json;
    }

    public static Test fromJson(Map<String, Object> json, Map<String, RuntimeVariable> variables) {

        String testVariableName = (String)json.get("test-variable");
        String actionString = (String)json.get("action");
        String type = (String)json.get("type");

        Map<String, Object> logic = ObjectUtil.cast(json.get("test-logic"));
        String testMetric = (String)logic.get("test-metric");
        String compare = (String)logic.get("test-compare");

        String description = (String)json.get("test-documentation");

        RuntimeVariable variable = variables.get(testVariableName);

        if (variable == null){
            variable = new RuntimeVariable(null, testVariableName);
        }

        return new Test(
            variable,
            TestOperator.fromName(compare),
            testMetric,
            TestAction.fromDefinition(actionString),
            MetricConversion.fromDefinition(type),
            description
        );
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        String oldDescription = this.description;
        this.description = description;
        this.firePropertyChange("description",oldDescription,description);
    }

}
