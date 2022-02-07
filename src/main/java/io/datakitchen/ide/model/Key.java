package io.datakitchen.ide.model;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public interface Key {

    void addKeyListener(KeyListener listener);
    void removeKeyListener(KeyListener listener);

    Connection getConnection();
    String getName();

    void addVariable(RuntimeVariable variable);
    void removeVariable(RuntimeVariable variable);
    Set<RuntimeVariable> getVariables();

    default Map<String, Object> toJson(){return new LinkedHashMap<>();}

    String getDescription();
}
