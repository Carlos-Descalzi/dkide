package io.datakitchen.ide.editors.neweditors;

import io.datakitchen.ide.model.Connection;
import io.datakitchen.ide.model.Key;

@FunctionalInterface
public interface KeyViewFactory{
    KeyView createKeyView(Connection connection, Key key);
}
