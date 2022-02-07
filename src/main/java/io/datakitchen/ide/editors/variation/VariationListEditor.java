package io.datakitchen.ide.editors.variation;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.util.Disposer;
import com.intellij.util.ui.JBUI;
import io.datakitchen.ide.editors.DocumentChangeEvent;
import io.datakitchen.ide.editors.DocumentChangeListener;
import io.datakitchen.ide.ui.EventSupport;
import io.datakitchen.ide.ui.ItemList;
import io.datakitchen.ide.util.ObjectUtil;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class VariationListEditor extends JPanel implements Disposable {

    private final EventSupport<DocumentChangeListener> eventSupport = EventSupport.of(DocumentChangeListener.class);
    private final ItemList<VariationItem> variationList = new ItemList<>(this::createVariation);
    private final VariationEditor variationEditor = new VariationEditor();
    private final DocumentChangeListener documentChangeListener = this::variationChanged;
    private final ListSelectionListener listSelectionListener = this::selectVariation;

    private Map<String, Object> variationDocument;
    private VariationItem currentVariation;
    private Function<VariationItem, VariationInfo> transferDataSupplier;

    public VariationListEditor(){

        setLayout(new BorderLayout());
        variationEditor.setBorder(JBUI.Borders.empty(10));
        add(variationList, BorderLayout.WEST);
        add(variationEditor, BorderLayout.CENTER);

        Disposer.register(this, variationList);

        enableEvents();
        updateActions();
    }

    private VariationItem createVariation() {
        return new VariationItem("variation-"+(variationList.getDataSize()+1));
    }

    @Override
    public void dispose() {

    }

    public Function<VariationItem, VariationInfo> getTransferDataSupplier() {
        return transferDataSupplier;
    }

    public void setTransferDataSupplier(Function<VariationItem, VariationInfo> transferDataSupplier) {
        this.transferDataSupplier = transferDataSupplier;
    }

    public void addDocumentChangeListener(DocumentChangeListener listener){
        this.eventSupport.addListener(listener);
    }

    public void removeDocumentChangeListener(DocumentChangeListener listener){
        this.eventSupport.removeListener(listener);
    }

    private void enableEvents(){
        variationEditor.addDocumentChangeListener(this.documentChangeListener);
        variationList.addListSelectionListener(this.listSelectionListener);
        variationList.addDocumentChangeListener(this.documentChangeListener);
    }

    private void disabledEvents(){
        variationEditor.removeDocumentChangeListener(this.documentChangeListener);
        variationList.removeListSelectionListener(this.listSelectionListener);
        variationList.removeDocumentChangeListener(this.documentChangeListener);
    }

    private void updateActions(){
        variationEditor.setEnabled(currentVariation != null);
    }

    private void selectVariation(ListSelectionEvent listSelectionEvent) {
        disabledEvents();
        if (currentVariation != null){
            variationEditor.saveVariation(currentVariation);
        }

        currentVariation = variationList.getSelected();

        if (currentVariation != null){
            variationEditor.loadVariation(currentVariation);
        }
        enableEvents();
        updateActions();
    }

    private void variationChanged(DocumentChangeEvent documentChangeEvent) {
        if (currentVariation != null){
            variationEditor.saveVariation(currentVariation);
        }
        saveDocument();
        eventSupport.getProxy().documentChanged(new DocumentChangeEvent(this));
    }
    public void updateOptions() {
        variationEditor.updateOptions();
    }

    public void setVariationDocument(Map<String, Object> variationDocument) {
        this.variationDocument = variationDocument;
        disabledEvents();
        Map<String,Object> variationList = ObjectUtil.cast(variationDocument.get("variation-list"));
        List<VariationItem> variationItems = new ArrayList<>();
        if (variationList != null) {
            variationItems.addAll(variationList
                    .entrySet()
                    .stream()
                    .map(VariationItem::fromEntry)
                    .map(i -> {
                        i.setTransferDataSupplier(transferDataSupplier);
                        return i;
                    })
                    .collect(Collectors.toList()));
        }
        this.variationList.setData(variationItems);

        variationEditor.setVariationDocument(variationDocument);
        currentVariation = null;
        variationEditor.loadVariation(null);
        enableEvents();
        updateActions();
        if (variationItems.size() > 0){
            this.variationList.setSelectedIndex(0);
        }
    }

    public void saveDocument() {

        Map<String, Object> variations = new LinkedHashMap<>();
        for (VariationItem item: variationList.getData()){
            variations.put(item.getName(),item.getVariation());
        }
        variationDocument.put("variation-list", variations);
    }


    public void notifyReferencedObjectAdded(String typeName, String itemName) {
        variationEditor.notifyReferencedObjectAdded(typeName, itemName);
    }

    public void notifyReferencedObjectRemoved(String typeName, String itemName) {
        variationEditor.notifyReferencedObjectRemoved(typeName, itemName);
    }

    public void notifyReferencedObjectChanged(String typeName, String itemName, String oldItemName) {
        variationEditor.notifyReferencedObjectChanged(typeName, itemName, oldItemName);
    }
}
