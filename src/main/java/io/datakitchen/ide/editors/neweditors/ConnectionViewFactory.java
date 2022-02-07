package io.datakitchen.ide.editors.neweditors;

import io.datakitchen.ide.model.Connection;

@FunctionalInterface
public interface ConnectionViewFactory {
    ConnectionView createConnectionView(ConnectionListView listView, Connection connection);
}
