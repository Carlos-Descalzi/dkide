package io.datakitchen.ide.model;

import java.util.List;

public interface ConnectionList {
    List<Connection> getConnections();
    void addConnectionForConnector(Connector connector);
    void addConnectionListListener(ConnectionListListener listener);
    void removeConnectionListListener(ConnectionListListener listener);
    void removeConnection(Connection connection);

}
