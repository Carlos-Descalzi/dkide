package io.datakitchen.ide.editors.neweditors.mapper;

import io.datakitchen.ide.model.ConnectionList;
import io.datakitchen.ide.model.ConnectorType;
import io.datakitchen.ide.model.DataMapperModel;
import io.datakitchen.ide.model.DataType;

public interface DataMapperConnectionList extends ConnectionList {
    DataMapperModel getModel();

    void addConnection(ConnectionImpl connection);

    DataType getDataType(ConnectorType connectorType);
}
