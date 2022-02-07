package io.datakitchen.ide.editors.neweditors;

import io.datakitchen.ide.model.Connection;

@FunctionalInterface
public interface RunRequestHandler {
    void runConnection(Connection connection);
}
