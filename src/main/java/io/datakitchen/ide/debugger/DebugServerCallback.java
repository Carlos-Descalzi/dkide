package io.datakitchen.ide.debugger;

public interface DebugServerCallback {
    void messageReceived(Message message);

    void processConnected();
}
