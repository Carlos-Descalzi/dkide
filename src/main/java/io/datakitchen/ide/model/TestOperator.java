package io.datakitchen.ide.model;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public enum TestOperator {

    EQUAL("equal-to", "=="),
    NOT_EQUAL("not-equal-to", "!="),
    GREATER_THAN("greater-than", ">"),
    GREATER_THAN_EQUAL("greater-than-equal-to", ">="),
    LESS_THAN("less-than", "<"),
    LESS_THAN_EQUAL("less-than-equal-to", "<=");

    private String name;
    private String displayName;

    private static final Map<String, TestOperator> BY_NAME = Arrays.stream(values())
            .collect(Collectors.toMap(TestOperator::getName,(TestOperator t)->t));

    TestOperator(String name, String displayName) {
        this.name = name;
        this.displayName = displayName;
    }

    public static TestOperator fromName(String name) {
        return BY_NAME.get(name);
    }

    public String getName() {
        return name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String toString(){
        return displayName;
    }
}
