package io.datakitchen.ide.model;

import java.util.EventObject;

public class ConnectionListEvent extends EventObject {

    private Connection connection;

    public ConnectionListEvent(Object source, Connection connection) {
        super(source);
        this.connection = connection;
    }

    public Connection getConnection(){
        return connection;
    }
}
