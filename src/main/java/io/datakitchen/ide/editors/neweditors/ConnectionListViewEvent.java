package io.datakitchen.ide.editors.neweditors;

import java.util.EventObject;

public class ConnectionListViewEvent extends EventObject {

    private final ConnectionView connectionView;

    public ConnectionListViewEvent(Object source, ConnectionView connectionView){
        super(source);
        this.connectionView = connectionView;
    }

    public ConnectionView getConnectionView() {
        return connectionView;
    }
}
