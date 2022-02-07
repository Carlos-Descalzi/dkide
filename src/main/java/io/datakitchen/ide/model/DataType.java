package io.datakitchen.ide.model;

import java.util.Set;

public interface DataType{

    ConnectorType getConnectorType();

    String getTypeName();

    Set<VariableDescription> getConnectorVariables();

    Set<VariableDescription> getKeyVariables();

}
