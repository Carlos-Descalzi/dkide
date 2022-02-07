package io.datakitchen.ide.model;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public enum QueryType {
    EXECUTE_QUERY("execute_query"),
    EXECUTE_SCALAR("execute_scalar"),
    EXECUTE_STATEMENT("execute_non_query");

    private String key;

    QueryType(String key) {
        this.key = key;
    }

    public String getKey() {
        return this.key;
    }

    private static final Map<String, QueryType> BY_KEY = Arrays
            .stream(values())
            .collect(Collectors.toMap(QueryType::getKey,(QueryType q)->q));

    public static QueryType fromKey(String k) {
        return BY_KEY.get(k);
    }

}
