package io.datakitchen.ide.editors.neweditors;

import io.datakitchen.ide.model.*;
import io.datakitchen.ide.util.ObjectUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.LinkedHashMap;
import java.util.Map;

public class FileTest extends Test {
    private final Key key;

    public FileTest(Key key, RuntimeVariable variable, TestOperator operator, String metricString, TestAction testAction) {
        this(key, variable, operator, metricString, testAction, null);
    }

    public FileTest(Key key, RuntimeVariable variable, TestOperator operator, String metricString, TestAction testAction, MetricConversion type) {
        this(key, variable, operator, metricString, testAction, type, null);
    }

    public FileTest(Key key, RuntimeVariable variable, TestOperator operator, String metricString, TestAction testAction, MetricConversion type, String description) {
        super(variable, operator, metricString, testAction, type, description);
        this.key = key;
    }

    public Key getKey(){
        return key;
    }

    public int hashCode(){

        return new HashCodeBuilder()
                .appendSuper(super.hashCode())
                .append(key)
                .toHashCode();
    }

    public boolean equals(Object other){
        return this == other || (other instanceof FileTest
                && new EqualsBuilder()
                .appendSuper(super.equals(other))
                .append(key, ((FileTest) other).key)
                .isEquals());
    }

    public String toString(){

        String name = key instanceof FileKey
                ? ((FileKey)key).getFile()
                : key.getName();

        return getVariable().getAttribute().getDisplayName()
                +" of "+name
                +" "+getOperator().getDisplayName()
                +" "+getMetricString();
    }

    public Map<String, Object> toJson() {
        Map<String, Object> json = new LinkedHashMap<>();
        json.put("test-variable", getVariable().getVariableName());
        json.put("action",getTestAction().getDefinition());

        if (StringUtils.isNotBlank(getDescription())){
            json.put("test-documentation", getDescription());
        }

        MetricConversion type = getType();
        if (type == null){
            type = getVariable().getAttribute().getType();
        }
        if (type == null){
            type = MetricConversion.DEFAULT;
        }

        json.put("type",type.getDefinition());

        Map<String, Object> logic = new LinkedHashMap<>();
        logic.put("test-compare", getOperator().getName());
        logic.put("test-metric", getMetricString());
        json.put("test-logic", logic);
        return json;
    }


    public static FileTest fromJson(Key key, Map<String, Object> json, Map<String, RuntimeVariable> variables) {

        String testVariableName = (String)json.get("test-variable");
        String actionString = (String)json.get("action");
        String description = (String)json.get("test-documentation");
        String type = (String)json.get("type");

        Map<String, Object> logic = ObjectUtil.cast(json.get("test-logic"));
        String testMetric = (String)logic.get("test-metric");
        String compare = (String)logic.get("test-compare");

        return new FileTest(
                key,
                variables.get(testVariableName),
                TestOperator.fromName(compare),
                testMetric,
                TestAction.fromDefinition(actionString),
                MetricConversion.fromDefinition(type),
                description
        );
    }

}
