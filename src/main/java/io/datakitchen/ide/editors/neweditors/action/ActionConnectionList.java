package io.datakitchen.ide.editors.neweditors.action;

import io.datakitchen.ide.model.ActionNodeModel;
import io.datakitchen.ide.model.ConnectionList;
import io.datakitchen.ide.model.ConnectorType;
import io.datakitchen.ide.model.DataType;

public interface ActionConnectionList extends ConnectionList {
    ActionNodeModel getModel();

    DataType getDataType(ConnectorType connectorType);

    void addConnection(ConnectionImpl connection);
}
