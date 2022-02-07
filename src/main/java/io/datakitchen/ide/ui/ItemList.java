package io.datakitchen.ide.ui;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.JBTable;
import io.datakitchen.ide.editors.DocumentChangeListener;
import io.datakitchen.ide.editors.DocumentEditor;
import io.datakitchen.ide.util.ObjectUtil;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ItemList<T extends NamedObject>
    extends JPanel
    implements
        Disposable,
        DocumentEditor,
        DragGestureListener,
        ClipboardOwner,
        FlavorListener{

    private static final Logger LOGGER = Logger.getInstance(ItemList.class);


    private final ListTableModel<T> model = new ListTableModel<>();
    private final JTable list = new JBTable();
    private final Action addAction = new SimpleAction(AllIcons.General.Add,"", "Add", this::doAdd);
    private final Action copyAction = new SimpleAction(AllIcons.General.InlineCopy,"","Clone", this::doCopy);
    private final Action removeAction = new SimpleAction(AllIcons.General.Remove,"", "Remove", this::doRemove);
    private final Action copyToClipboardAction = new SimpleAction(AllIcons.Actions.Copy,"","Copy To Clipboard", this::copyToClipboard);
    private final Action pasteToClipboardAction = new SimpleAction(AllIcons.Actions.MenuPaste,"","Paste From Clipboard", this::pasteFromClipboard);
    private final Action moveUpAction = new SimpleAction(AllIcons.General.ArrowUp, "","Move up",this::moveUp);
    private final Action moveDownAction = new SimpleAction(AllIcons.General.ArrowDown, "","Move down",this::moveDown);
    private final EventSupport<ListSelectionListener> eventSupport = EventSupport.of(ListSelectionListener.class);
    private final EventSupport<DocumentChangeListener> docEventSupport = EventSupport.of(DocumentChangeListener.class);
    private final EventSupport<ItemListListener> listeners = EventSupport.of(ItemListListener.class);
    private final ListSelectionListener listener = this::itemSelected;

    private final Supplier<T> itemFactory;
    private DataFlavor supportedFlavor = null;
    private Consumer<Transferable> pasteHandler = this::defaultPaste;

    public ItemList(Supplier<T> itemFactory){
        this(itemFactory, false);
    }

    public ItemList(Supplier<T> itemFactory, boolean showOrderActions){
        this.itemFactory = itemFactory;
        setLayout(new BorderLayout());
        JScrollPane scroll = new JBScrollPane(list, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scroll.setPreferredSize(new Dimension(150,200));
        add(scroll, BorderLayout.CENTER);

        list.setAutoCreateColumnsFromModel(false);
        list.getColumnModel().addColumn(new TableColumn(0));
        list.setModel(model);
        list.getSelectionModel().setSelectionMode(DefaultListSelectionModel.SINGLE_SELECTION);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT,2,5));
        buttons.add(makeButton(addAction));
        buttons.add(makeButton(copyAction));
        buttons.add(makeButton(removeAction));
        buttons.add(makeButton(copyToClipboardAction));
        buttons.add(makeButton(pasteToClipboardAction));
        if (showOrderActions){
            buttons.add(makeButton(moveUpAction));
            buttons.add(makeButton(moveDownAction));
        }
        add(buttons, BorderLayout.SOUTH);
        updateActions();
        enableEvents();
        DragSource dragSource = new DragSource();
        dragSource.createDefaultDragGestureRecognizer(list, DnDConstants.ACTION_COPY,this);
        getToolkit().getSystemClipboard().addFlavorListener(this);
        setMinimumSize(new Dimension(200,200));
    }

    private JButton makeButton(Action action) {
        JButton b = new JButton(action);
        b.setPreferredSize(new Dimension(32,28));
        return b;
    }

    public void setRenderer(TableCellRenderer renderer){
        this.list.getColumnModel().getColumn(0).setCellRenderer(renderer);
    }

    public void setEditor(TableCellEditor editor){
        this.list.getColumnModel().getColumn(0).setCellEditor(editor);
    }

    private void itemSelected(ListSelectionEvent event) {
        updateActions();
        this.eventSupport.getProxy().valueChanged(new ListSelectionEvent(this,event.getFirstIndex(),event.getLastIndex(),event.getValueIsAdjusting()));
    }

    public void addListSelectionListener(ListSelectionListener listener){
        this.eventSupport.addListener(listener);
    }

    public void removeListSelectionListener(ListSelectionListener listener){
        this.eventSupport.removeListener(listener);
    }
    public void addDocumentChangeListener(DocumentChangeListener listener){
        this.docEventSupport.addListener(listener);
    }

    public void removeDocumentChangeListener(DocumentChangeListener listener){
        this.docEventSupport.removeListener(listener);
    }

    @Override
    public JComponent getEditorComponent() {
        return this;
    }

    public DataFlavor getSupportedFlavor() {
        return supportedFlavor;
    }

    public void setSupportedFlavor(DataFlavor supportedFlavor) {
        this.supportedFlavor = supportedFlavor;
        updateActions();
    }

    public Consumer<Transferable> getPasteHandler() {
        return pasteHandler;
    }

    public void setPasteHandler(Consumer<Transferable> pasteHandler) {
        this.pasteHandler = pasteHandler != null ? pasteHandler : this::defaultPaste;
    }

    @Override
    public void dispose() {
        getToolkit().getSystemClipboard().removeFlavorListener(this);
    }

    @Override
    public void flavorsChanged(FlavorEvent e) {
        updateActions();
    }

    private void doCopy(ActionEvent event) {
        T item = model.getItem(this.list.getSelectedRow());
        try {
            item = ObjectUtil.copy(item);
            item.setName(item.getName()+"_copy");
            this.model.add(item);
            updateActions();
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    private void doAdd(ActionEvent e){
        T item = this.itemFactory.get();
        this.model.add(item);
        updateActions();
    }

    private void doRemove(ActionEvent e){
        int index = this.list.getSelectedRow();
        if (index != -1){
            this.model.remove(index);
        }
        updateActions();
    }
    private void copyToClipboard(ActionEvent event) {
        T item = model.getItem(list.getSelectedRow());
        if (item instanceof Transferable){
            getToolkit().getSystemClipboard().setContents((Transferable) item, this);
        }
    }

    private void pasteFromClipboard(ActionEvent event) {
        if (supportedFlavor != null) {
            Transferable transferable = getToolkit().getSystemClipboard().getContents(this);
            if (transferable.isDataFlavorSupported(supportedFlavor)){
                pasteHandler.accept(transferable);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void defaultPaste(Transferable transferable){
        try {
            T newItem = (T) transferable.getTransferData(supportedFlavor);
            model.add(newItem);
        }catch(Exception ex){
            LOGGER.error(ex);
        }
    }

    public void addItem(T item){
        int index = model.getRowCount();
        this.model.add(item);
        this.list.getSelectionModel().setSelectionInterval(index,index);
        updateActions();
    }

    private void updateActions(){
        boolean enabled = isEnabled();
        copyAction.setEnabled(enabled && list.getSelectedRow() != -1);
        removeAction.setEnabled(enabled && list.getSelectedRow() != -1);
        addAction.setEnabled(enabled);
        moveUpAction.setEnabled(enabled && list.getSelectedRow() > 0);
        moveDownAction.setEnabled(
            enabled
            && list.getSelectedRow() != -1
            && list.getSelectedRow() < model.getRowCount()-1
        );
        list.setEnabled(enabled);
        copyToClipboardAction.setEnabled(
            enabled
            && list.getSelectedRow() != -1
            && list.getValueAt(list.getSelectedRow(),0) instanceof Transferable
        );

        if (enabled && supportedFlavor != null) {
            Transferable transferable = getToolkit().getSystemClipboard().getContents(this);
            pasteToClipboardAction.setEnabled(transferable.isDataFlavorSupported(supportedFlavor));
        } else {
            pasteToClipboardAction.setEnabled(false);
        }
    }

    private void enableEvents(){
        list.getSelectionModel().addListSelectionListener(this.listener);
    }

    private void disableEvents(){
        list.getSelectionModel().removeListSelectionListener(this.listener);
    }

    public void setData(List<T> data){
        disableEvents();
        this.model.setData(data);
        this.list.clearSelection();
        enableEvents();
        updateActions();
    }

    public void setEnabled(boolean enabled){
        super.setEnabled(enabled);
        updateActions();
    }
    private void moveUp(ActionEvent event) {
        int index = list.getSelectedRow();
        model.decreasePosition(index);
        updateActions();
    }
    private void moveDown(ActionEvent event) {
        int index = list.getSelectedRow();
        model.increasePosition(index);
        updateActions();
    }


    public List<T> getData(){
        return model.getData();
    }

    public T getSelected() {
        return model.getItem (list.getSelectedRow());
    }

    public void setSelectedIndex(int index) {
        this.list.getSelectionModel().setSelectionInterval(index, index);
    }

    public int getDataSize() {
        return this.list.getModel().getRowCount();
    }

    @Override
    public void dragGestureRecognized(DragGestureEvent dge) {
        T item = model.getItem(list.getSelectedRow());
        if (item instanceof Transferable){
            dge.startDrag(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR),(Transferable) item);
        }
    }

    @Override
    public void lostOwnership(Clipboard clipboard, Transferable contents) {

    }
    private class ListTableModel<TT extends NamedObject> extends AbstractTableModel{
        private List<T> items = new ArrayList<>();

        public List<T> getData(){
            return new ArrayList<>(items);
        }

        public void setData(List<T> data){
            this.items = new ArrayList<>(data);
            this.fireTableDataChanged();
        }

        @Override
        public int getRowCount() {
            return items.size();
        }

        @Override
        public int getColumnCount() {
            return 1;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            return items.get(rowIndex);
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return true;
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            T item = this.items.get(rowIndex);
            String oldName = item.getName();
            item.setName((String)aValue);
            fireTableCellUpdated(rowIndex, columnIndex);
            listeners.getProxy().itemChanged(new ItemListEvent(ItemList.this, item, oldName));
        }

        public void add(T item) {
            int index = items.size();
            items.add(item);
            fireTableRowsInserted(index, index);
            listeners.getProxy().itemAdded(new ItemListEvent(ItemList.this, item));
        }

        public T getItem(int index){
            if (index >=0 && index < items.size()) {
                return items.get(index);
            }
            return null;
        }

        public void remove(int index) {
            T item = items.get(index);
            items.remove(index);
            fireTableRowsDeleted(index, index);
            listeners.getProxy().itemRemoved(new ItemListEvent(ItemList.this, item));
        }

        public void decreasePosition(int index) {
            T item = items.remove(index);
            items.add(index-1,item);
            fireTableRowsUpdated(index-1,index);
        }

        public void increasePosition(int index) {
            T item = items.remove(index);
            items.add(index+1, item);
            fireTableRowsUpdated(index,index+1);
        }
    }

    public void addItemListListener(ItemListListener listener){
        listeners.addListener(listener);
    }

    public void removeItemListListener(ItemListListener listener){
        listeners.removeListener(listener);
    }

}
