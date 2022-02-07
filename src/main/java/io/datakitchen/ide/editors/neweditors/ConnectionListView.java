package io.datakitchen.ide.editors.neweditors;

import io.datakitchen.ide.model.*;
import io.datakitchen.ide.ui.EventSupport;
import io.datakitchen.ide.ui.VerticalStackLayout;
import org.apache.commons.lang.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.dnd.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.TooManyListenersException;

public class ConnectionListView extends JPanel implements DropTargetListener, ConnectionListListener {

    private final Map<Connection, ConnectionView> viewMap = new HashMap<>();
    private final EventSupport<ConnectionListViewListener> listeners = EventSupport.of(ConnectionListViewListener.class);

    public KeyViewFactory keyViewFactory;
    private ConnectionList connectionList;
    private ConnectionViewFactory viewFactory = ConnectionView::new;
    private String emptyText;
    private RunRequestHandler runRequestHandler;

    public ConnectionListView(){
        setOpaque(false);
        DropTarget dropTarget = new DropTarget();
        try {
            dropTarget.addDropTargetListener(this);
        }catch(TooManyListenersException ignored){}
        dropTarget.setActive(true);
        setDropTarget(dropTarget);
        setLayout(new VerticalStackLayout(5));
    }

    public void setEmptyText(String emptyText){
        this.emptyText = emptyText;
        repaint();
    }

    public void addConnectionListViewListener(ConnectionListViewListener listener){
        this.listeners.addListener(listener);
    }

    public void removeConnectionListViewListener(ConnectionListViewListener listener){
        this.listeners.removeListener(listener);
    }

    public RunRequestHandler getRunRequestHandler() {
        return runRequestHandler;
    }

    public void setRunRequestHandler(RunRequestHandler runRequestHandler) {
        this.runRequestHandler = runRequestHandler;
        for (ConnectionView view:getConnectionViews()){
            view.setRunRequestHandler(runRequestHandler);
        }
    }

    private ConnectionView[] getConnectionViews() {
        return Arrays.stream(getComponents()).map(c -> (ConnectionView)c).toArray(ConnectionView[]::new);
    }

    public ConnectionList getConnectionList() {
        return connectionList;
    }

    public void setConnectionList(ConnectionList connectionList){
        if (this.connectionList != null){
            this.connectionList.removeConnectionListListener(this);
        }
        this.connectionList = connectionList;
        if (this.connectionList != null){
            this.connectionList.addConnectionListListener(this);
        }
        loadConnections();
    }

    public KeyViewFactory getKeyViewFactory() {
        return keyViewFactory;
    }

    public void setKeyViewFactory(KeyViewFactory keyViewFactory) {
        this.keyViewFactory = keyViewFactory;
    }

    public ConnectionViewFactory getViewFactory() {
        return viewFactory;
    }

    public void setViewFactory(ConnectionViewFactory viewFactory) {
        this.viewFactory = viewFactory != null ? viewFactory : ConnectionView::new;
    }

    public Point getHookForNewFileEntry(Connection connection) {
        ConnectionView view = viewMap.get(connection);
        return view.getHookForNewFileEntry();
    }

    @Override
    public void dragEnter(DropTargetDragEvent dtde) {
        if (dtde.isDataFlavorSupported(Connector.FLAVOR)){
            dtde.acceptDrag(DnDConstants.ACTION_COPY);
        } else {
            dtde.rejectDrag();
        }
    }

    @Override
    public void dragOver(DropTargetDragEvent dtde) {}

    @Override
    public void dropActionChanged(DropTargetDragEvent dtde) {}

    @Override
    public void dragExit(DropTargetEvent dte) {}

    @Override
    public void drop(DropTargetDropEvent dtde) {
        dtde.acceptDrop(DnDConstants.ACTION_COPY);
        try {
            Connector connector = (Connector) dtde.getTransferable().getTransferData(Connector.FLAVOR);
            connectionList.addConnectionForConnector(connector);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ConnectionView findConnectorByName(String name) {
        for (Component c:getComponents()){
            ConnectionView slot = (ConnectionView) c;
            if (slot.getConnection().getName().equals(name)){
                return slot;
            }
        }
        return null;
    }

    @Override
    public void connectionAdded(ConnectionListEvent event) {
        addConnection(event.getConnection());
    }

    @Override
    public void connectionRemoved(ConnectionListEvent event) {
        ConnectionView view = viewMap.remove(event.getConnection());
        remove(view);
        validate();
        listeners.getProxy().connectionViewRemoved(new ConnectionListViewEvent(this, view));
    }

    private void loadConnections(){
        removeAll();
        if (connectionList != null) {
            for (Connection connection : connectionList.getConnections()) {
                addConnection(connection);
            }
        }
    }

    private void addConnection(Connection connection){
        ConnectionView view = viewFactory.createConnectionView(this, connection);
        if (runRequestHandler != null){
            view.setRunRequestHandler(runRequestHandler);
        }
        if (keyViewFactory != null) {
            view.setFileViewFactory(keyViewFactory);
        }
        view.build();
        add(view);
        viewMap.put(connection,view);
        revalidate();
        repaint();
        listeners.getProxy().connectionViewAdded(new ConnectionListViewEvent(this, view));
    }

    protected void paintComponent(Graphics g){
        if (StringUtils.isNotBlank(emptyText) && getComponentCount() == 0){
            ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            String[] lines = emptyText.split("\n");

            Font font = getFont();
            font = font.deriveFont((float)font.getSize()-2);
            g.setFont(font);

            FontMetrics fm = g.getFontMetrics(font);
            int totalHeight = fm.getHeight()*lines.length;

            int y = (getHeight()-totalHeight)/2;

            for (int i=0;i<lines.length;i++){
                String line = lines[i].strip();
                int w = fm.stringWidth(line);
                g.drawString(line,(getWidth()-w)/2,y);
                y+=fm.getHeight();
            }

        }
    }
}
