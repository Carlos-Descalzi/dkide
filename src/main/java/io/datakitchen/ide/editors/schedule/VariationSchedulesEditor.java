package io.datakitchen.ide.editors.schedule;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.util.Disposer;
import io.datakitchen.ide.editors.*;
import io.datakitchen.ide.ui.*;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class VariationSchedulesEditor extends JPanel implements Disposable, VariationItemEditor {

    private final EventSupport<VariationEditionListener> editionListeners = EventSupport.of(VariationEditionListener.class);
    private final ItemList<ScheduleItem> schedules = new ItemList<>(this::createSchedule);
    private final ScheduleEditor scheduleEditor = new ScheduleEditor();
    private final EventSupport<DocumentChangeListener> eventSupport = EventSupport.of(DocumentChangeListener.class);
    private final DocumentChangeListener scheduleEditorListener = this::scheduleChanged;
    private final ListSelectionListener listSelectionListener = this::scheduleSelected;
    private final ItemListListener itemListListener = new ItemListListener() {
        @Override
        public void itemAdded(ItemListEvent event) {
            notifyItemAdded(event);
        }

        @Override
        public void itemRemoved(ItemListEvent event) {
            notifyItemRemoved(event);
        }

        @Override
        public void itemChanged(ItemListEvent event) {
            notifyItemChanged(event);
        }
    };

    private Map<String, Object> variationDocument;
    private ScheduleItem currentSchedule;

    public VariationSchedulesEditor(){
        setLayout(new BorderLayout());

        scheduleEditor.setBorder(UIUtil.EMPTY_BORDER_10x10);
        schedules.setSupportedFlavor(ScheduleItem.FLAVOR);
        add(schedules,BorderLayout.WEST);
        add(scheduleEditor,BorderLayout.CENTER);
        Disposer.register(this, schedules);
        enableEvents();
        updateActions();

    }

    public void addDocumentChangeListener(DocumentChangeListener listener){
        this.eventSupport.addListener(listener);
    }
    public void removeDocumentChangeListener(DocumentChangeListener listener){
        this.eventSupport.removeListener(listener);
    }

    private ScheduleItem createSchedule(){
        return new ScheduleItem("schedule-"+(schedules.getDataSize()+1));
    }

    @Override
    public void dispose() {

    }

    private void enableEvents(){
        scheduleEditor.addDocumentChangeListener(scheduleEditorListener);
        schedules.addListSelectionListener(listSelectionListener);
        schedules.addDocumentChangeListener(scheduleEditorListener);
        schedules.addItemListListener(itemListListener);
    }

    private void disableEvents(){
        scheduleEditor.removeDocumentChangeListener(scheduleEditorListener);
        schedules.removeListSelectionListener(listSelectionListener);
        schedules.removeDocumentChangeListener(scheduleEditorListener);
        schedules.removeItemListListener(itemListListener);
    }

    private void scheduleSelected(ListSelectionEvent listSelectionEvent) {
        disableEvents();
        if (currentSchedule != null){
            scheduleEditor.saveSchedule(currentSchedule);
        }

        currentSchedule = schedules.getSelected();
        scheduleEditor.loadSchedule(currentSchedule);
        updateActions();
        enableEvents();
    }



    private void updateActions(){
        scheduleEditor.setEnabled(currentSchedule != null);
    }

    public void setVariationDocument(Map<String, Object> variationDocument) {
        disableEvents();
        this.variationDocument = variationDocument;
        if (variationDocument != null){
            Map<String,Object> schedules = (Map<String,Object>) variationDocument.get("schedule-setting-list");
            if (schedules == null){
                // backwards compatibility
                schedules = (Map<String,Object>) variationDocument.get("mesos-setting-list");
            }
            List<ScheduleItem> items = new ArrayList<>();
            if (schedules != null) {
                items.addAll(
                        schedules
                                .entrySet()
                                .stream()
                                .map(ScheduleItem::fromEntry)
                                .collect(Collectors.toList())
                );
            }
            this.schedules.setData(items);
        }
        currentSchedule = null;
        this.scheduleEditor.loadSchedule(null);
        enableEvents();
        updateActions();
        if (this.schedules.getDataSize() > 0){
            this.schedules.setSelectedIndex(0);
        }
    }

    public void saveDocument() {
        Map<String, Object> schedules = new LinkedHashMap<>();

        for (ScheduleItem item: this.schedules.getData()){
            schedules.put(item.getName(), item.getValue());
        }
        variationDocument.put("schedule-setting-list", schedules);
        variationDocument.remove("mesos-setting-list");
    }


    private void scheduleChanged(DocumentChangeEvent documentChangeEvent) {
        schedules.repaint();
        if (currentSchedule != null){
            scheduleEditor.saveSchedule(currentSchedule);
        }
        saveDocument();
        eventSupport.getProxy().documentChanged(new DocumentChangeEvent(this));
    }

    public ScheduleItem getScheduleByName(String scheduleName) {
        return schedules.getData()
            .stream().filter(s -> s.getName().equals(scheduleName))
            .findFirst().orElse(null);
    }

    @Override
    public void addVariationEditionListener(VariationEditionListener listener) {
        this.editionListeners.addListener(listener);
    }

    @Override
    public void removeVariationEditionListener(VariationEditionListener listener) {
        this.editionListeners.removeListener(listener);
    }

    private void notifyItemAdded(ItemListEvent event){
        editionListeners.getProxy().variationItemAdded(
                new VariationEditionEvent(this,"schedule",event.getItem().getName())
        );
    }
    private void notifyItemRemoved(ItemListEvent event){
        editionListeners.getProxy().variationItemRemoved(
                new VariationEditionEvent(this,"schedule",event.getItem().getName())
        );
    }
    private void notifyItemChanged(ItemListEvent event){
        editionListeners.getProxy().variationItemChanged(
                new VariationEditionEvent(this,"schedule",event.getItem().getName(), event.getOldName())
        );
    }
}
