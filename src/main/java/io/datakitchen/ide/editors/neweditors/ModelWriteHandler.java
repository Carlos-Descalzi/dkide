package io.datakitchen.ide.editors.neweditors;

import io.datakitchen.ide.model.*;

public class ModelWriteHandler implements ConnectionListListener, ConnectionListener, KeyListener {

    private final Runnable onChange;

    public ModelWriteHandler(Runnable onChange){
        this.onChange = onChange;
    }

    protected void setup(ConnectionList connectionList){
        connectionList.addConnectionListListener(this);
        for (Connection c: connectionList.getConnections()){
            c.addConnectionListener(this);
            for (Key k: c.getKeys()){
                k.addKeyListener(this);
            }
        }
    }

    @Override
    public void connectionAdded(ConnectionListEvent event) {
        onChange.run();
        event.getConnection().addConnectionListener(this);
    }

    @Override
    public void connectionRemoved(ConnectionListEvent event) {
        onChange.run();
        event.getConnection().removeConnectionListener(this);
    }

    @Override
    public void nameChanged(ConnectionEvent event) {
        onChange.run();
    }

    @Override
    public void testAdded(ConnectionEvent event) {
        onChange.run();
    }

    @Override
    public void testChanged(ConnectionEvent event) {
        onChange.run();
    }

    @Override
    public void testRemoved(ConnectionEvent event) {
        onChange.run();
    }

    @Override
    public void variableAdded(ConnectionEvent event) {
        onChange.run();
    }

    @Override
    public void variableRemoved(ConnectionEvent event) {
        onChange.run();
    }

    @Override
    public void keyAdded(ConnectionEvent event) {
        onChange.run();
        event.getKey().addKeyListener(this);
    }

    @Override
    public void keyRemoved(ConnectionEvent event) {
        onChange.run();
        event.getKey().removeKeyListener(this);
    }

    @Override
    public void variableAdded(KeyEvent event) {
        onChange.run();
    }

    @Override
    public void variableRemoved(KeyEvent event) {
        onChange.run();
    }

    @Override
    public void keyChanged(KeyEvent event) {
        onChange.run();
    }
}
