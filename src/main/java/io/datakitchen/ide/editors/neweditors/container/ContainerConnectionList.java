package io.datakitchen.ide.editors.neweditors.container;

import io.datakitchen.ide.model.ConnectionList;
import io.datakitchen.ide.model.ConnectorType;
import io.datakitchen.ide.model.ContainerModel;
import io.datakitchen.ide.model.DataType;

public interface ContainerConnectionList extends ConnectionList {
    ContainerModel getModel();

    void addConnection(ConnectionImpl connection);

    DataType getDataType(ConnectorType connectorType);
}
