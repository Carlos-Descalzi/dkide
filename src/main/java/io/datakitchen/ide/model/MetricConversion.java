package io.datakitchen.ide.model;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public enum MetricConversion {
    INTEGER("test-contents-as-integer","Integer"),
    STRING("test-contents-as-string","String"),
    FLOAT("test-contents-as-float","Float"),
    DATE("test-contents-as-date","Date"),
    BOOLEAN("test-contents-as-boolean","Boolean"),
    DEFAULT("test-contents-as-default","Default"),
    ;

    private static final Map<String, MetricConversion> BY_DEFINITION = Arrays.stream(values())
            .collect(Collectors.toMap(MetricConversion::getDefinition, ((MetricConversion m)->m)));

    public static MetricConversion fromDefinition(String definition) {
        return BY_DEFINITION.get(definition);
    }

    private final String definition;
    private final String description;

    MetricConversion(String definition, String description){
        this.definition = definition;
        this.description = description;
    }

    public String toString(){
        return description;
    }

    public String getDefinition() {
        return definition;
    }

    public String getDescription() {
        return description;
    }
}
