package io.datakitchen.ide.model;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public enum TestAction {
    LOG("test-log.png", "log", "Log"),
    WARN("test-warning.png", "warning","Warning"),
    ERROR("test-error.png","stop-on-error","Error");

    private String iconName;
    private String definition;
    private String description;

    private static final Map<String, TestAction> BY_DEFINITION = Arrays.stream(values())
            .collect(Collectors.toMap(TestAction::getDefinition,(TestAction a)->a));

    TestAction(String iconName, String definition, String description){
        this.iconName = iconName;
        this.definition = definition;
        this.description = description;
    }

    public static TestAction fromDefinition(String definition) {
        return BY_DEFINITION.get(definition);
    }

    public String toString(){
        return description;
    }

    public String getIconName(){
        return iconName;
    }

    public String getDefinition(){
        return definition;
    }
}
